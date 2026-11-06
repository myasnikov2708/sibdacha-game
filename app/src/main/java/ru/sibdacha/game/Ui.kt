package ru.sibdacha.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.max
import kotlin.math.roundToInt

val Ink = Color(0xFF263238)
val Grass = Color(0xFF9CCC65)
val Soil = Color(0xFF8D6E63)
val Dry = Color(0xFFBCAAA4)
val Leaf = Color(0xFF48702F)
val Panel = Color(0xFFF6F8F0)
val Bg = Color(0xFFEDF2E4)
val Sky = Color(0xFF35505F)
val Gold = Color(0xFFFFE082)
val SnowC = Color(0xFFE3F2FD)

@Composable
fun App(s: GameState, open: (String) -> Unit) {
    var tab by remember { mutableStateOf(0) }

    if (s.inExpedition) {
        ExpeditionScreen(s)
        return
    }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> FarmScreen(s, open)
                1 -> ShopScreen(s, open)
                else -> JournalScreen(s, open)
            }
        }
        Row(Modifier.fillMaxWidth().background(Color(0xFF2E4230))) {
            val titles = listOf("Участок", "Лавка", "Дневник")
            titles.forEachIndexed { i, title ->
                TextButton(onClick = { tab = i }, modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        color = if (tab == i) Color.White else Color(0xFF93A88E),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun Header(s: GameState) {
    Surface(color = if (s.season == Season.WINTER) Color(0xFF37474F) else Sky) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text("Сибирская дача", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(
                "Год ${s.year} · ${s.season.label}, ${s.dom}-й день",
                color = Color(0xFFCFE0D8), fontSize = 12.sp
            )
            Text(
                "Деньги: ${s.money} ₽    Силы: ${s.energy}    Склад: " +
                    "%.1f".format(s.storage.values.sum()) + " кг",
                color = Color(0xFFE8F1EA), fontSize = 12.sp
            )
        }
    }
}

fun harvestLeft(s: GameState, p: Plant): Int = (p.ripeDay + 3) - s.day

fun tvLine(s: GameState): String {
    val out = mutableListOf<String>()

    if (s.frostDay > s.day) out.add("заморозок на ${domOf(s.frostDay)}-й день")
    if (s.winterKnown) out.add("зима " + s.winter.label)
    if (s.season == Season.WINTER) out.add("камеральный сезон")

    var uncover = false
    var thirsty = false
    var expiring = 0

    s.plantsWithTiles().forEach { (_, p) ->
        if (p.covered && p.uncoverDay in 0..(s.day + 3)) uncover = true
        if (p.alive && s.day - p.lastWatered >= Crops.byId(p.cropId).thirstDays - 3) thirsty = true
        if (p.ripeKg > 0.0 && harvestLeft(s, p) <= 1) expiring++
    }

    if (uncover) out.add("снять укрытия")
    if (thirsty) out.add("растения просят воды")
    if (expiring > 0) out.add("урожай пропадёт: $expiring")
    if (out.isEmpty()) out.add("спокойный день")

    return "ТВ: " + out.joinToString(" · ")
}

@Composable
fun FarmScreen(s: GameState, open: (String) -> Unit) {
    var picked by remember { mutableStateOf(-1) }
    var market by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Header(s)

        Surface(color = Color(0xFF24343C)) {
            Text(
                tvLine(s),
                color = Color(0xFFA8D5C8),
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        LazyColumn(Modifier.weight(1f).padding(8.dp)) {
            item {
                s.log.takeLast(3).reversed().forEach {
                    Text(it, fontSize = 11.sp, color = Ink, modifier = Modifier.padding(vertical = 1.dp))
                }
                Spacer(Modifier.height(8.dp))
            }

            items(ROWS) { r ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    for (c in 0 until COLS) {
                        val index = r * COLS + c
                        Box(Modifier.weight(1f)) {
                            TileBox(s, index) { picked = it }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(10.dp)) }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { market = true },
                modifier = Modifier.weight(1.2f),
                colors = ButtonDefaults.buttonColors(containerColor = Leaf)
            ) { Text("Рынок", fontSize = 12.sp) }

            OutlinedButton(onClick = { open(Links.CATALOG) }, modifier = Modifier.weight(1f)) {
                Text("Каталог", fontSize = 12.sp)
            }
        }

        Button(
            onClick = { Engine.nextDay(s) },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Sky)
        ) {
            Text("Следующий день · ${s.dom} ${s.season.label}")
        }
    }

    if (picked >= 0) ActionSheet(s, picked) { picked = -1 }
    if (market) MarketDialog(s) { market = false }
}

@Composable
fun TileBox(s: GameState, index: Int, onPick: (Int) -> Unit) {
    val tile = s.tiles[index]
    val anchor = s.anchor(index)
    val owner = s.tiles[anchor]
    val p = owner.plant
    val isPart = tile.partOf >= 0
    val winter = s.season == Season.WINTER

    val color = when {
        tile.blocked -> Color(0xFF4E342E)
        p == null && !tile.tilled -> if (winter) SnowC else Grass
        p == null && tile.tilled -> if (winter) Color(0xFFCFD8DC) else Dry
        p != null && !p.alive -> Color(0xFF9E9E9E)
        p != null && p.ripeKg > 0.0 -> Gold
        else -> Soil
    }

    val label = when {
        tile.blocked -> "Х"
        isPart -> ""
        p == null && !tile.tilled -> ""
        p == null -> "."
        !p.alive -> "X"
        else -> Crops.byId(p.cropId).emoji
    }

    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(color, RoundedCornerShape(5.dp))
            .clickable { onPick(anchor) },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = if (isPart) 10.sp else 15.sp)

        if (!isPart && p != null) {
            if (p.covered) {
                Text("шапка", fontSize = 7.sp, modifier = Modifier.align(Alignment.TopCenter))
            }
                        if (p.ripeKg > 0.0) {
                Text(
                    "${harvestLeft(s, p)}",
                    fontSize = 8.sp,
                    color = Color(0xFF6A1B9A),
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }

        if (s.heroTile == index) {
            Text("Я", fontSize = 9.sp, color = Color.White, modifier = Modifier.align(Alignment.BottomStart))
        }
    }
}

@Composable
fun ActionSheet(s: GameState, index: Int, dismiss: () -> Unit) {
    val tile = s.tiles.getOrNull(index) ?: return
    val p = tile.plant
    var plantMode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = dismiss) {
        Surface(shape = RoundedCornerShape(14.dp), color = Panel) {
            LazyColumn(
                Modifier.padding(14.dp).heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    Text(
                        "Грядка ${index / COLS + 1}-${index % COLS + 1}",
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        fontSize = 15.sp
                    )

                    if (p != null) {
                        val c = Crops.byId(p.cropId)
                        Text("${c.emoji} ${c.name}", fontSize = 13.sp)
                        Text(
                            "возраст ${p.age} г. · плоды с ${c.firstYear} г. · " +
                                "полив каждые ${c.thirstDays} дн. · место ${c.size}×${c.size}",
                            fontSize = 11.sp
                        )

                        if (p.ripeKg > 0.0) {
                            Text(
                                "К сбору: " + "%.1f".format(p.ripeKg) + " кг · осталось " +
                                    "${harvestLeft(s, p)} дн.",
                                fontSize = 12.sp,
                                color = Color(0xFF6A1B9A)
                            )
                        }

                        if (p.disease > 0f) {
                            Text("Болезни: −${(p.disease * 100).roundToInt()}%", fontSize = 11.sp)
                        }
                        if (p.frostLoss > 0f) {
                            Text(
                                "Потери от заморозка: −${(p.frostLoss * 100).roundToInt()}%",
                                fontSize = 11.sp
                            )
                        }
                        if (p.nitrogen) Text("Азот внесён: +20%", fontSize = 11.sp, color = Leaf)
                        if (p.pk) Text("Фосфор-калий внесён: +20%", fontSize = 11.sp, color = Leaf)
                        if (c.needsPair && !Engine.pollinated(s, p)) {
                            Text(
                                "Нет второго сорта-опылителя — урожай будет мал.",
                                fontSize = 11.sp,
                                color = Color(0xFFC62828)
                            )
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                }

                if (plantMode) {
                    val owned = s.seedlings.filter { it.value > 0 }

                    if (owned.isEmpty()) {
                        item { Text("Саженцев нет — купите в лавке.", fontSize = 12.sp) }
                    } else {
                        items(owned.entries.toList()) { e ->
                            val c = Crops.byId(e.key)
                            TextButton(
                                onClick = {
                                    Engine.act(s, index, Action.PLANT, e.key)
                                    dismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${c.emoji} ${c.name} (${e.value})", fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        TextButton(onClick = { plantMode = false }) {
                            Text("Назад", fontSize = 12.sp)
                        }
                    }
                } else {
                    items(Engine.actions(s, index)) { a ->
                        TextButton(
                            onClick = {
                                if (a == Action.PLANT) {
                                    plantMode = true
                                } else {
                                    Engine.act(s, index, a)
                                    dismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${a.label} · ${a.energy} сил", fontSize = 12.sp)
                        }
                    }
                }

                item { HorizontalDivider(Modifier.padding(vertical = 6.dp)) }
                item {
                    TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Закрыть", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MarketDialog(s: GameState, dismiss: () -> Unit) {
    Dialog(onDismissRequest = dismiss) {
        Surface(shape = RoundedCornerShape(14.dp), color = Panel) {
            LazyColumn(
                Modifier.padding(14.dp).heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text("Рынок", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                    Text(
                        "Цена падает на 4% за каждые 25 кг проданного вида за сезон. " +
                            "Минимум — 55% от базовой.",
                        fontSize = 10.sp
                    )
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                }

                if (s.storage.isEmpty()) {
                    item { Text("Склад пуст.", fontSize = 12.sp) }
                } else {
                    items(s.storage.entries.toList()) { e ->
                        if (e.value > 0.01) {
                            val c = Crops.byId(e.key)
                            val now = Engine.marketPrice(s, e.key)
                            val factorAfter = max(
                                0.55,
                                Engine.marketFactor(s, e.key) - 0.04 * (e.value / 25.0)
                            )
                            val after = (c.priceKg * factorAfter).roundToInt()

                            Column(Modifier.padding(vertical = 3.dp)) {
                                Text("${c.emoji} ${c.name}", fontSize = 12.sp)
                                Text(
                                    "%.1f".format(e.value) + " кг × $now ₽ сейчас → после продажи $after ₽",
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    Text(
                        "Итого: ${Engine.storageTotal(s)} ₽",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Leaf
                    )
                }

                item {
                    Button(
                        onClick = {
                            Engine.sellAll(s)
                            dismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Leaf)
                    ) { Text("Продать всё") }
                }

                item {
                    TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Закрыть", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ShopScreen(s: GameState, open: (String) -> Unit) {
    var bushes by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize()) {
        Header(s)

        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = bushes,
                onClick = { bushes = true },
                label = { Text("Ягодники", fontSize = 11.sp) }
            )
            FilterChip(
                selected = !bushes,
                onClick = { bushes = false },
                label = { Text("Деревья 2×2", fontSize = 11.sp) }
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { open(Links.CATALOG) }) {
                Text("Каталог", fontSize = 11.sp)
            }
        }

        LazyColumn(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            items(Crops.all.filter { it.tree != bushes }) { c ->
                val owned = s.seedlings[c.id] ?: 0
                Card(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Column(Modifier.padding(10.dp)) {
                        Text(
                            "${c.emoji} ${c.name}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${c.price} ₽ · плоды с ${c.firstYear} г. · до ${c.maxKg} кг · " +
                                "${c.priceKg} ₽/кг" +
                                if (c.needsPair) " · нужен второй сорт" else "",
                            fontSize = 10.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { Engine.buy(s, c) }) {
                                Text("Купить", fontSize = 11.sp)
                            }
                            TextButton(onClick = { open(c.url) }) {
                                Text("Сорта на сайте", fontSize = 11.sp)
                            }
                            if (owned > 0) {
                                Text("в сумке: $owned", fontSize = 10.sp, color = Leaf)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                Text("Постройки", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink)
            }

            items(Buildings.all) { b ->
                val built = s.buildings[b.id] == true
                Card(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Row(
                        Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(b.name, fontSize = 13.sp)
                            Text("${b.price} ₽ · ${b.effect}", fontSize = 10.sp)
                        }
                        if (built) {
                            Text("есть", fontSize = 11.sp, color = Leaf)
                        } else {
                            TextButton(onClick = { Engine.buyBuilding(s, b) }) {
                                Text("Строить", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
fun JournalScreen(s: GameState, open: (String) -> Unit) {
    val era = Eras.all[s.eraIndex.coerceIn(0, Eras.all.lastIndex)]
    val winter = s.season == Season.WINTER

    Column(Modifier.fillMaxSize()) {
        Header(s)

        LazyColumn(Modifier.weight(1f).padding(10.dp)) {
            item {
                Text("Навыки", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink)
                Text(
                    "Сила ${s.skillForce} · Знание ${s.skillKnowledge} · " +
                        "Терпение ${s.skillPatience}",
                    fontSize = 12.sp
                )
                Text(
                    "Вскопано: ${s.tillCount} · находок: ${s.artCount} · " +
                        "реставраций: ${s.restorations}",
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(10.dp))
            }

            if (winter) {
                item {
                    Text("Зимние работы", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink)
                    Text(
                        "Раскопки замерли до весны — время обрабатывать находки и читать архив.",
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(6.dp))

                    Button(
                        onClick = { Engine.restoreArtifact(s) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                    ) { Text("Реставрация находки · 2 силы", fontSize = 12.sp) }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = { Engine.readArchive(s) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                    ) { Text("Работа в архиве · 1 сила", fontSize = 12.sp) }

                    Spacer(Modifier.height(10.dp))
                }
            }

            item {
                Text("Раскоп: ${era.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink)
                Text("Пласт ${s.eraLevel} из 20 · ${era.years}", fontSize = 11.sp)
                Text("Противники: ${era.enemies.joinToString(", ")}", fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))

                Button(
                    onClick = { Engine.startExpedition(s) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E)),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Спуститься в курган · $EXPEDITION_COST сил", fontSize = 12.sp) }

                Spacer(Modifier.height(10.dp))
            }

            if (s.artifacts.isNotEmpty()) {
                item {
                    Text("Находки", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink)
                }
                items(s.artifacts) { a -> Text("· $a", fontSize = 11.sp) }
                item {
                    TextButton(onClick = { Engine.clearFind(s) }) {
                        Text("Оформить находку (освободить грядку)", fontSize = 11.sp)
                    }
                }
            }

            if (s.notes.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Записи об эпохах", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink)
                }
                items(s.notes) { n -> Text("· $n", fontSize = 11.sp) }
            }

            item {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = { open(Links.HOME) }, modifier = Modifier.fillMaxWidth()) {
                    Text("сибирскаядача.рф", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatBar(value: Int, maximum: Int, color: Color) {
    val fraction = if (maximum <= 0) 0f else (value.toFloat() / maximum.toFloat()).coerceIn(0f, 1f)
    Box(
        Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(Color(0xFF2A2A2A), RoundedCornerShape(5.dp))
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .background(color, RoundedCornerShape(5.dp))
        )
    }
}

@Composable
fun ExpeditionScreen(s: GameState) {
    val era = Eras.all[s.eraIndex.coerceIn(0, Eras.all.lastIndex)]
    val stats = listOf("Сила", "Знание", "Терпение")

    Column(Modifier.fillMaxSize().background(Color(0xFF1E1A17)).padding(14.dp)) {
        Text(
            "Раскоп · ${era.name}",
            color = Color(0xFFFFE0B2),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text("Пласт ${s.eraLevel} из 20 · ${era.years}", color = Color(0xFFBCAAA4), fontSize = 11.sp)
        Spacer(Modifier.height(10.dp))

        Text("Противник: ${s.enemyName}", color = Color.White, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        StatBar(s.enemyHp, s.enemyMax, Color(0xFFC62828))
        Spacer(Modifier.height(10.dp))

        Text("Целостность контекста: ${s.context}", color = Color(0xFFFFCC80), fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        StatBar(s.context, 100, Color(0xFF8D6E63))
        Spacer(Modifier.height(8.dp))

        Text(
            "Свет: ${s.light} ходов · Силы: ${s.energy}",
            color = Color(0xFFFFF59D),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(12.dp))

        Text(
            "Слабость противника — ${stats[s.enemyWeak].lowercase()}",
            color = Color(0xFF80CBC4),
            fontSize = 11.sp
        )
        Spacer(Modifier.height(8.dp))

        stats.forEachIndexed { idx, title ->
            Button(
                onClick = { Engine.attack(s, idx) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E))
            ) { Text("$title · $ATTACK_COST сила", fontSize = 13.sp) }
        }

        Spacer(Modifier.height(10.dp))

        LazyColumn(Modifier.weight(1f)) {
            items(s.log.takeLast(8).reversed()) {
                Text(
                    it,
                    color = Color(0xFFD7CCC8),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        OutlinedButton(
            onClick = { s.inExpedition = false },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Выйти наверх", fontSize = 12.sp) }
    }
}
