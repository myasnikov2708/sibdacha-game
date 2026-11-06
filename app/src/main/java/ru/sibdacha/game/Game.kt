package ru.sibdacha.game

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.maxOf
import kotlin.math.minOf
import kotlin.math.roundToInt
import kotlin.random.Random

// ─────────────────── ОСНОВНЫЕ КОНСТАНТЫ ───────────────────

const val COLS = 8
const val ROWS = 6
const val SLOTS = COLS * ROWS
const val SEASON_DAYS = 30
const val YEAR_DAYS = 120
const val RENT = 1500
const val EXPEDITION_COST = 25
const val ATTACK_COST = 1

enum class Season(val label: String) {
    SPRING("весна"),
    SUMMER("лето"),
    AUTUMN("осень"),
    WINTER("зима")
}

fun seasonOf(day: Int): Season = Season.values()[(day / SEASON_DAYS).coerceIn(0, 3)]

fun domOf(day: Int): Int = day % SEASON_DAYS + 1

/**
 * Адреса сайта. Если кнопки не открывают сайт — скопируйте адрес
 * из адресной строки браузера и вставьте сюда.
 */
object Links {
    const val HOME = "https://сибирскаядача.рф"
    const val CATALOG = "https://сибирскаядача.рф/catalog"
}

// ─────────────────── КУЛЬТУРЫ ───────────────────

data class CropDef(
    val id: String,
    val species: String,
    val name: String,
    val emoji: String,
    val price: Int,
    val firstYear: Int,
    val curve: List<Int>,
    val harvestDays: List<Int>,
    val maxKg: Double,
    val priceKg: Int,
    val thirstDays: Int,
    val tree: Boolean,
    val size: Int,
    val needsPair: Boolean,
    val winterDeath: Int,
    val winterPenalty: Int,
    val snowRot: Int,
    val thorns: Boolean,
    val url: String
)

object Crops {

    private fun term(
        base: String,
        title: String,
        emoji: String,
        price: Int,
        firstYear: Int,
        curve: List<Int>,
        days: List<Int>,
        maxKg: Double,
        priceKg: Int,
        thirst: Int,
        slug: String,
        tree: Boolean,
        size: Int = 1,
        pair: Boolean = false,
        winterDeath: Int = 0,
        winterPenalty: Int = 0,
        snowRot: Int = 0
    ): List<CropDef> {
        val names = listOf("ранний", "средний", "поздний")
        val suffix = listOf("rannie", "srednie", "pozdnie")
        return (0..2).map { i ->
            CropDef(
                id = "$base-${suffix[i]}",
                species = base,
                name = "$title, ${names[i]}",
                emoji = emoji,
                price = price,
                firstYear = firstYear,
                curve = curve,
                harvestDays = listOf(days[i]),
                maxKg = maxKg,
                priceKg = priceKg,
                thirstDays = thirst,
                tree = tree,
                size = size,
                needsPair = pair,
                winterDeath = winterDeath,
                winterPenalty = winterPenalty,
                snowRot = snowRot,
                thorns = false,
                url = "${Links.CATALOG}/$slug/${suffix[i]}"
            )
        }
    }

    private fun one(
        id: String,
        name: String,
        emoji: String,
        price: Int,
        firstYear: Int,
        curve: List<Int>,
        harvest: List<Int>,
        maxKg: Double,
        priceKg: Int,
        thirst: Int,
        slug: String,
        species: String,
        size: Int = 1,
        pair: Boolean = false,
        winterDeath: Int = 0,
        winterPenalty: Int = 0,
        snowRot: Int = 0,
        thorns: Boolean = false
    ): CropDef = CropDef(
        id = id,
        species = species,
        name = name,
        emoji = emoji,
        price = price,
        firstYear = firstYear,
        curve = curve,
        harvestDays = harvest,
        maxKg = maxKg,
        priceKg = priceKg,
        thirstDays = thirst,
        tree = false,
        size = size,
        needsPair = pair,
        winterDeath = winterDeath,
        winterPenalty = winterPenalty,
        snowRot = snowRot,
        thorns = thorns,
        url = "${Links.CATALOG}/$slug"
    )

    val all: List<CropDef> = buildList {

        addAll(term("apple", "Яблоня", "🍎", 1200, 3, listOf(30, 60, 100),
            listOf(49, 60, 74), 50.0, 55, 30, "yablonya", true, 2, winterDeath = 30))

        addAll(term("apple-semi", "Яблоня-полукультурка", "🍏", 1200, 2, listOf(30, 60, 100),
            listOf(44, 60, 74), 60.0, 75, 30, "yablonya-polukulturnaya", true, 2, winterPenalty = 10))

        addAll(term("pear", "Груша", "🍐", 1400, 2, listOf(30, 60, 100),
            listOf(44, 60, 79), 35.0, 110, 30, "grusha", true, 2, pair = true, winterPenalty = 10))

        addAll(term("plum", "Слива", "🟣", 1500, 2, listOf(30, 60, 100),
            listOf(39, 49, 54), 20.0, 90, 30, "sliva", true, 2, pair = true, snowRot = 30))

        addAll(term("apricot", "Абрикос", "🍑", 1500, 3, listOf(30, 60, 100),
            listOf(39, 49, 54), 15.0, 140, 30, "abrikos", true, 2, pair = true,
            winterDeath = 20, snowRot = 20))

        addAll(term("cherry", "Вишня", "🍒", 1000, 2, listOf(30, 80, 100),
            listOf(39, 49, 54), 7.0, 160, 30, "vishnya", true, 2, pair = true))

        addAll(term("currant", "Смородина", "🫐", 500, 1, listOf(30, 80, 100),
            listOf(39, 49, 54), 3.5, 200, 30, "smorodina", false))

        addAll(term("gooseberry", "Крыжовник", "🟢", 500, 1, listOf(30, 80, 100),
            listOf(39, 49, 54), 4.5, 170, 30, "kryzhovnik", false))

        addAll(term("honeysuckle", "Жимолость", "🔵", 800, 2, listOf(30, 40, 80, 100),
            listOf(39, 49, 54), 2.0, 400, 30, "zhimolost", false, pair = true))

        addAll(term("grape", "Виноград", "🍇", 800, 2, listOf(30, 40, 80, 100),
            listOf(39, 49, 54), 5.0, 220, 15, "vinograd", false, winterDeath = 50))

        add(one("oblepiha", "Облепиха", "🟠", 800, 2, listOf(30, 40, 80, 100),
            listOf(49, 54), 10.0, 150, 30, "oblepiha", "oblepiha", pair = true))

        add(one("oblepiha-male", "Облепиха мужская", "🌿", 600, 99, listOf(0),
            emptyList(), 0.0, 0, 30, "oblepiha", "oblepiha"))

        add(one("raspberry", "Малина крупноплодная", "🔴", 250, 2, listOf(50, 100),
            listOf(39), 2.0, 280, 15, "malina", "raspberry", winterDeath = 30))

        add(one("raspberry-rem", "Малина ремонтантная", "🔴", 400, 1, listOf(50, 100),
            listOf(39, 60), 2.5, 280, 15, "malina-remontantnaya", "raspberry"))

        add(one("raspberry-bush", "Малина кустовая", "🔴", 500, 2, listOf(50, 100),
            listOf(39, 44, 49, 54, 60, 64, 69), 3.5, 280, 15,
            "malina-kustovaya", "raspberry", winterDeath = 15))

        add(one("blackberry", "Ежевика", "⚫", 800, 2, listOf(50, 100),
            listOf(39), 3.5, 320, 15, "ezhevika", "blackberry", winterDeath = 30))

        add(one("strawberry", "Земляника крупноплодная", "🍓", 150, 1, listOf(50, 100),
            listOf(39), 0.7, 400, 30, "zemlyanika", "strawberry", winterDeath = 30))

        add(one("strawberry-rem", "Земляника ремонтантная", "🍓", 220, 1, listOf(50, 100),
            listOf(39, 60), 0.9, 400, 15, "zemlyanika-remontantnaya", "strawberry", winterDeath = 30))

        add(one("blueberry", "Голубика", "🫐", 900, 1, listOf(30, 60, 100),
            listOf(39, 60), 1.5, 450, 15, "golubika", "blueberry", winterDeath = 30))

        add(one("gooseberry-thorn", "Крыжовник с шипами", "🟢", 400, 1, listOf(30, 80, 100),
            listOf(39, 49, 54), 4.5, 170, 30, "kryzhovnik", "gooseberry", thorns = true))
    }

    fun byId(id: String): CropDef = all.firstOrNull { it.id == id } ?: all[0]
}

// ─────────────────── ПОСТРОЙКИ ───────────────────

data class Building(val id: String, val name: String, val price: Int, val effect: String)

object Buildings {
    val all = listOf(
        Building("drip", "Капельный полив", 8000, "полив не тратит силы"),
        Building("cellar", "Погреб", 6000, "продажа дороже на 15%"),
        Building("greenhouse", "Теплица", 25000, "сад защищён от заморозков"),
        Building("compost", "Компостная яма", 3000, "саженцы дешевле на 15%")
    )
}

// ─────────────────── ЭПОХИ ───────────────────

data class Era(val name: String, val years: String, val enemies: List<String>, val note: String)

/**
 * Черновые тексты. Перед публикацией сверить
 * с Хакасским национальным краеведческим музеем им. Л. Р. Кызласова.
 */
object Eras {
    val all = listOf(
        Era("Афанасьевская культура", "конец IV — начало III тыс. до н. э.",
            listOf("Осыпь", "Сыпучий грунт", "Грызун"),
            "Афанасьевская культура — одна из древнейших скотоводческих культур Минусинской котловины. (на сверку с музеем)"),
        Era("Окуневская культура", "III тыс. до н. э.",
            listOf("Каменная личина", "Плита ограды", "Ветер"),
            "Окуневская культура известна стелами с личинами; плиты вторично использовались в поздних оградах. (на сверку с музеем)"),
        Era("Андроновская культура", "II тыс. до н. э.",
            listOf("Грунтовые воды", "Ледяная линза", "Крепь"),
            "Андроновские памятники дают керамику с геометрическим орнаментом. (на сверку с музеем)"),
        Era("Тагарская культура", "VIII–III вв. до н. э.",
            listOf("Зверь звериного стиля", "Мёрзлый слой", "Огонь"),
            "Тагарская культура — время больших курганов и звериного стиля. (на сверку с музеем)"),
        Era("Таштыкская культура", "I тыс. н. э.",
            listOf("Погребальная маска", "Пыль", "Эхо"),
            "Таштыкские погребальные маски — один из самых узнаваемых материалов региона. (на сверку с музеем)")
    )
}

// ─────────────────── МОДЕЛЬ ДАННЫХ ───────────────────

enum class WinterWeather(val label: String) {
    MILD("мягкая и снежная"),
    COLD("морозная и малоснежная"),
    SNOWY("многоснежная")
}

class Plant(val cropId: String) {
    var age by mutableStateOf(1)
    var lastWatered by mutableStateOf(0)
    var covered by mutableStateOf(false)
    var uncoverDay by mutableStateOf(-1)
    var frostLoss by mutableStateOf(0f)
    var disease by mutableStateOf(0f)
    var nitrogen by mutableStateOf(false)
    var pk by mutableStateOf(false)
    var treated by mutableStateOf(false)
    var whitewashed by mutableStateOf(false)
    var winterCover by mutableStateOf(false)
    var snowCleared by mutableStateOf(false)
    var ripeKg by mutableStateOf(0.0)
    var ripeDay by mutableStateOf(-1)
    var alive by mutableStateOf(true)
}

class TileData {
    var tilled by mutableStateOf(false)
    var blocked by mutableStateOf(false)
    var partOf by mutableStateOf(-1)
    var plant by mutableStateOf<Plant?>(null)
}

class GameState {
    var day by mutableStateOf(0)
    var year by mutableStateOf(1)
    var money by mutableStateOf(12000)
    var energy by mutableStateOf(100)
    var frostDay by mutableStateOf(-1)
    var winter by mutableStateOf(WinterWeather.MILD)
    var winterKnown by mutableStateOf(false)
    var heroTile by mutableStateOf(0)
    var skillForce by mutableStateOf(1)
    var skillKnowledge by mutableStateOf(1)
    var skillPatience by mutableStateOf(1)
    var tillCount by mutableStateOf(0)
    var artCount by mutableStateOf(0)
    var eraLevel by mutableStateOf(1)
    var eraIndex by mutableStateOf(0)
    var inExpedition by mutableStateOf(false)
    var enemyName by mutableStateOf("Осыпь")
    var enemyHp by mutableStateOf(1)
    var enemyMax by mutableStateOf(1)
    var enemyWeak by mutableStateOf(0)
    var context by mutableStateOf(100)
    var light by mutableStateOf(6)
    var restorations by mutableStateOf(0)
    var archiveRead by mutableStateOf(0)

    val tiles = mutableStateListOf<TileData>().apply { repeat(SLOTS) { add(TileData()) } }
    val seedlings = mutableStateMapOf<String, Int>()
    val storage = mutableStateMapOf<String, Double>()
    val sold = mutableStateMapOf<String, Double>()
    val artifacts = mutableStateListOf<String>()
    val notes = mutableStateListOf<String>()
    val buildings = mutableStateMapOf<String, Boolean>()
    val log = mutableStateListOf<String>()

    val season: Season get() = seasonOf(day)
    val dom: Int get() = domOf(day)

    fun say(text: String) {
        if (text.isBlank()) return
        log.add(text)
        while (log.size > 60) log.removeAt(0)
    }

    fun spend(cost: Int): Boolean {
        if (energy < cost) {
            say("Не хватает сил — закончите день.")
            return false
        }
        energy -= cost
        return true
    }

    fun anchor(index: Int): Int {
        val t = tiles.getOrNull(index) ?: return index
        return if (t.partOf >= 0) t.partOf else index
    }

    fun plantsWithTiles(): List<Pair<Int, Plant>> {
        val result = mutableListOf<Pair<Int, Plant>>()
        tiles.forEachIndexed { i, t ->
            if (t.partOf < 0) {
                val p = t.plant
                if (p != null) result.add(i to p)
            }
        }
        return result
    }
}

// ─────────────────── СОХРАНЕНИЕ ───────────────────

object Save {
    private const val PREFS = "sibdacha"
    private const val KEY = "state"
    private const val VERSION = 3

    private fun clean(v: String) = v.replace("|", "/").replace("\n", " ")

    fun save(ctx: Context, s: GameState) {
        val sb = StringBuilder()
        sb.append("v|").append(VERSION).append('\n')

        sb.append("meta")
            .append('|').append(s.day)
            .append('|').append(s.year)
            .append('|').append(s.money)
            .append('|').append(s.energy)
            .append('|').append(s.frostDay)
            .append('|').append(s.winter.name)
            .append('|').append(if (s.winterKnown) 1 else 0)
            .append('|').append(s.heroTile)
            .append('|').append(s.skillForce)
            .append('|').append(s.skillKnowledge)
            .append('|').append(s.skillPatience)
            .append('|').append(s.tillCount)
            .append('|').append(s.artCount)
            .append('|').append(s.eraLevel)
            .append('|').append(s.eraIndex)
            .append('|').append(s.restorations)
            .append('|').append(s.archiveRead)
            .append('\n')

        s.tiles.forEachIndexed { i, t ->
            sb.append("tile")
                .append('|').append(i)
                .append('|').append(if (t.tilled) 1 else 0)
                .append('|').append(if (t.blocked) 1 else 0)
                .append('|').append(t.partOf)

            val p = t.plant
            if (p == null) {
                sb.append("|0\n")
            } else {
                sb.append("|1")
                    .append('|').append(p.cropId)
                    .append('|').append(p.age)
                    .append('|').append(p.lastWatered)
                    .append('|').append(if (p.covered) 1 else 0)
                    .append('|').append(p.uncoverDay)
                    .append('|').append(p.frostLoss)
                    .append('|').append(p.disease)
                    .append('|').append(if (p.nitrogen) 1 else 0)
                    .append('|').append(if (p.pk) 1 else 0)
                    .append('|').append(if (p.treated) 1 else 0)
                    .append('|').append(if (p.whitewashed) 1 else 0)
                    .append('|').append(if (p.winterCover) 1 else 0)
                    .append('|').append(if (p.snowCleared) 1 else 0)
                    .append('|').append(p.ripeKg)
                    .append('|').append(p.ripeDay)
                    .append('|').append(if (p.alive) 1 else 0)
                    .append('\n')
            }
        }

        s.seedlings.forEach { (k, v) -> sb.append("seed|").append(k).append('|').append(v).append('\n') }
        s.storage.forEach { (k, v) -> sb.append("stor|").append(k).append('|').append(v).append('\n') }
        s.sold.forEach { (k, v) -> sb.append("sold|").append(k).append('|').append(v).append('\n') }
        s.buildings.forEach { (k, v) -> if (v) sb.append("bld|").append(k).append('\n') }
        s.artifacts.forEach { sb.append("art|").append(clean(it)).append('\n') }
        s.notes.forEach { sb.append("note|").append(clean(it)).append('\n') }
        s.log.takeLast(25).forEach { sb.append("log|").append(clean(it)).append('\n') }

        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, sb.toString())
            .apply()
    }

    fun load(ctx: Context): GameState? {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return null

        val lines = raw.split('\n').filter { it.isNotBlank() }
        if (lines.isEmpty()) return null

        val head = lines[0].split('|')
        if (head.size < 2 || head[0] != "v") return null
        if (head[1].toIntOrNull() != VERSION) return null

        return try {
            val s = GameState()
            s.seedlings.clear()
            s.storage.clear()
            s.sold.clear()
            s.buildings.clear()
            s.artifacts.clear()
            s.notes.clear()
            s.log.clear()

            for (line in lines.drop(1)) {
                val v = line.split('|')
                when (v[0]) {
                    "meta" -> {
                        s.day = v[1].toInt()
                        s.year = v[2].toInt()
                        s.money = v[3].toInt()
                        s.energy = v[4].toInt()
                        s.frostDay = v[5].toInt()
                        s.winter = try {
                            WinterWeather.valueOf(v[6])
                        } catch (e: Exception) {
                            WinterWeather.MILD
                        }
                        s.winterKnown = v[7] == "1"
                        s.heroTile = v[8].toInt()
                        s.skillForce = v[9].toInt()
                        s.skillKnowledge = v[10].toInt()
                        s.skillPatience = v[11].toInt()
                        s.tillCount = v[12].toInt()
                        s.artCount = v[13].toInt()
                        s.eraLevel = v[14].toInt()
                        s.eraIndex = v[15].toInt()
                        s.restorations = v[16].toInt()
                        s.archiveRead = v[17].toInt()
                    }

                    "tile" -> {
                        val index = v[1].toInt()
                        val t = s.tiles.getOrNull(index) ?: continue
                        t.tilled = v[2] == "1"
                        t.blocked = v[3] == "1"
                        t.partOf = v[4].toInt()

                        if (v[5] == "1") {
                            val p = Plant(v[6])
                            p.age = v[7].toInt()
                            p.lastWatered = v[8].toInt()
                            p.covered = v[9] == "1"
                            p.uncoverDay = v[10].toInt()
                            p.frostLoss = v[11].toFloat()
                            p.disease = v[12].toFloat()
                            p.nitrogen = v[13] == "1"
                            p.pk = v[14] == "1"
                            p.treated = v[15] == "1"
                            p.whitewashed = v[16] == "1"
                            p.winterCover = v[17] == "1"
                            p.snowCleared = v[18] == "1"
                            p.ripeKg = v[19].toDouble()
                            p.ripeDay = v[20].toInt()
                            p.alive = v[21] == "1"
                            t.plant = p
                        } else {
                            t.plant = null
                        }
                    }

                    "seed" -> s.seedlings[v[1]] = v[2].toInt()
                    "stor" -> s.storage[v[1]] = v[2].toDouble()
                    "sold" -> s.sold[v[1]] = v[2].toDouble()
                    "bld" -> s.buildings[v[1]] = true
                    "art" -> s.artifacts.add(v[1])
                    "note" -> s.notes.add(v[1])
                    "log" -> s.log.add(v[1])
                }
            }
            s
        } catch (e: Exception) {
            null
        }
    }
}

// ─────────────────── ДЕЙСТВИЯ ───────────────────

enum class Action(val label: String, val energy: Int) {
    TILL("Копать", 3),
    PLANT("Посадить", 2),
    WATER("Полить", 1),
    COVER("Укрыть", 1),
    UNCOVER("Снять укрытие", 0),
    HARVEST("Собрать", 1),
    REMOVE("Убрать", 1),
    NITROGEN("Азот", 1),
    PK("Фосфор-калий", 1),
    TREAT("Обработка", 2),
    WHITEWASH("Побелка", 1),
    WINTER_COVER("Укрыть на зиму", 2),
    SNOW_CLEAR("Откопать снег", 2)
}

// ─────────────────── ДВИЖОК ───────────────────

object Engine {

    fun footprint(anchor: Int, size: Int): List<Int> {
        if (size <= 1) return listOf(anchor)
        val r = anchor / COLS
        val c = anchor % COLS
        if (c + 1 >= COLS || r + 1 >= ROWS) return emptyList()
        return listOf(anchor, anchor + 1, anchor + COLS, anchor + COLS + 1)
    }

    fun canPlace(s: GameState, anchor: Int, size: Int): Boolean {
        val cells = footprint(anchor, size)
        if (cells.isEmpty()) return false
        for (i in cells) {
            val t = s.tiles[i]
            if (t.plant != null || t.blocked || t.partOf != -1) return false
        }
        return true
    }

    fun pollinated(s: GameState, p: Plant): Boolean {
        val c = Crops.byId(p.cropId)
        if (!c.needsPair) return true

        val others = s.plantsWithTiles().map { it.second }.filter { it.alive }

        if (c.species == "oblepiha") {
            return others.any { Crops.byId(it.cropId).id == "oblepiha-male" }
        }

        val kinds = others
            .map { it.cropId }
            .filter { it != "oblepiha-male" }
            .filter { Crops.byId(it).species == c.species }
            .distinct()

        return kinds.size >= 2
    }

    fun marketFactor(s: GameState, cropId: String): Double {
        val already = s.sold[cropId] ?: 0.0
        return maxOf(0.55, 1.0 - 0.04 * (already / 25.0))
    }

    fun marketPrice(s: GameState, cropId: String): Int {
        val c = Crops.byId(cropId)
        val bonus = if (s.buildings["cellar"] == true) 1.15 else 1.0
        return (c.priceKg * marketFactor(s, cropId) * bonus).roundToInt()
    }

    fun storageTotal(s: GameState): Int {
        var total = 0.0
        s.storage.forEach { (id, kg) ->
            if (kg > 0.01) total += kg * marketPrice(s, id)
        }
        return total.roundToInt()
    }

    fun harvestLeft(s: GameState, p: Plant): Int = (p.ripeDay + 3) - s.day

    fun actions(s: GameState, index: Int): List<Action> {
        val i = s.anchor(index)
        val t = s.tiles.getOrNull(i) ?: return emptyList()
        val p = t.plant
        val winter = s.season == Season.WINTER
        val out = mutableListOf<Action>()

        if (p == null) {
            if (winter) return out
            if (!t.tilled && !t.blocked) out.add(Action.TILL)
            if (t.tilled) out.add(Action.PLANT)
            return out
        }

        if (!p.alive) {
            out.add(Action.REMOVE)
            return out
        }

        if (winter) {
            if (Crops.byId(p.cropId).snowRot > 0 && !p.snowCleared) out.add(Action.SNOW_CLEAR)
            return out
        }

        out.add(Action.WATER)
        if (p.covered) out.add(Action.UNCOVER) else out.add(Action.COVER)
        if (p.ripeKg > 0.0) out.add(Action.HARVEST)

        if (s.season == Season.SPRING && !p.nitrogen) out.add(Action.NITROGEN)
        if (s.season == Season.AUTUMN && s.dom >= 20 && !p.pk) out.add(Action.PK)
        if (s.season == Season.AUTUMN && s.dom >= 25 && !p.treated) out.add(Action.TREAT)
        if (s.season == Season.AUTUMN && s.dom >= 25 &&
            Crops.byId(p.cropId).tree && !p.whitewashed
        ) out.add(Action.WHITEWASH)
        if (s.season == Season.AUTUMN && !p.winterCover) out.add(Action.WINTER_COVER)

        return out
    }

    fun act(s: GameState, index: Int, action: Action, cropId: String? = null) {
        val i = s.anchor(index)
        val t = s.tiles.getOrNull(i) ?: return
        val p = t.plant

        if (s.season == Season.WINTER &&
            (action == Action.TILL || action == Action.WATER || action == Action.COVER ||
                action == Action.PLANT || action == Action.NITROGEN || action == Action.PK)
        ) {
            s.say("Зимой это недоступно.")
            return
        }

        val message: String = when (action) {

            Action.TILL -> {
                if (t.blocked) {
                    "Здесь находка — оформите её в дневнике."
                } else if (t.tilled) {
                    "Уже вскопано."
                } else if (!s.spend(Action.TILL.energy)) {
                    ""
                } else {
                    t.tilled = true
                    s.tillCount++
                    if (s.tillCount % 5 == 0) s.skillForce++
                    if (Random.nextInt(100) < 5) {
                        t.blocked = true
                        s.artCount++
                        val era = Eras.all[s.eraIndex.coerceIn(0, Eras.all.lastIndex)]
                        s.artifacts.add("${era.name}: находка №${s.artCount}")
                        "Находка! ${era.name}. Оформите в дневнике."
                    } else {
                        "Вскопали грядку."
                    }
                }
            }

            Action.PLANT -> {
                val def = if (cropId == null) null else Crops.byId(cropId)
                when {
                    def == null -> "Выберите саженец."
                    !t.tilled -> "Сначала вскопайте."
                    t.plant != null -> "Место занято."
                    (s.seedlings[def.id] ?: 0) <= 0 -> "Нет саженцев."
                    !canPlace(s, i, def.size) -> "Нужны свободные клетки ${def.size}×${def.size}."
                    !s.spend(Action.PLANT.energy) -> ""
                    else -> {
                        s.seedlings[def.id] = (s.seedlings[def.id] ?: 0) - 1
                        t.plant = Plant(def.id).also { it.lastWatered = s.day }
                        if (def.size > 1) {
                            for (cell in footprint(i, def.size)) {
                                if (cell != i) {
                                    s.tiles[cell].partOf = i
                                    s.tiles[cell].tilled = true
                                }
                            }
                        }
                        "Посажено: ${def.name}."
                    }
                }
            }

            Action.WATER -> when {
                p == null -> "Поливать нечего."
                !p.alive -> "Растение погибло."
                s.buildings["drip"] == true -> {
                    p.lastWatered = s.day
                    "Полили (капельный полив)."
                }
                !s.spend(Action.WATER.energy) -> ""
                else -> {
                    p.lastWatered = s.day
                    "Полили."
                }
            }

            Action.COVER -> when {
                p == null -> "Укрывать нечего."
                p.covered -> "Уже укрыто."
                !s.spend(Action.COVER.energy) -> ""
                else -> {
                    p.covered = true
                    "Укрыли спанбондом."
                }
            }

            Action.UNCOVER -> when {
                p == null -> ""
                !p.covered -> "Не укрыто."
                else -> {
                    p.covered = false
                    if (p.uncoverDay >= 0 && s.day <= p.uncoverDay) s.skillPatience++
                    p.uncoverDay = -1
                    "Укрытие снято."
                }
            }

            Action.HARVEST -> when {
                p == null || !p.alive -> "Нечего собирать."
                p.ripeKg <= 0.0 -> "Ещё не поспело."
                !s.spend(if (Crops.byId(p.cropId).thorns) 3 else 1) -> ""
                else -> {
                    val c = Crops.byId(p.cropId)
                    val kg = p.ripeKg
                    s.storage[c.id] = (s.storage[c.id] ?: 0.0) + kg
                    p.ripeKg = 0.0
                    p.ripeDay = -1
                    "Собрано " + "%.1f".format(kg) + " кг. " + c.name
                }
            }

            Action.REMOVE -> {
                if (p != null) {
                    val def = Crops.byId(p.cropId)
                    if (def.size > 1) {
                        for (cell in footprint(i, def.size)) s.tiles[cell].partOf = -1
                    }
                }
                t.plant = null
                t.tilled = true
                "Растение убрано."
            }

            Action.NITROGEN -> when {
                p == null -> ""
                p.nitrogen -> "Уже подкормлено."
                s.season != Season.SPRING -> "Азот вносят весной."
                s.money < 100 -> "Удобрение стоит 100 ₽."
                !s.spend(1) -> ""
                else -> {
                    s.money -= 100
                    p.nitrogen = true
                    "+20% к урожаю этого года."
                }
            }

            Action.PK -> when {
                p == null -> ""
                p.pk -> "Уже внесено."
                s.season != Season.AUTUMN || s.dom < 20 -> "Фосфор-калий — осенью после сбора."
                s.money < 150 -> "Удобрение стоит 150 ₽."
                !s.spend(1) -> ""
                else -> {
                    s.money -= 150
                    p.pk = true
                    "+20% к урожаю следующего года."
                }
            }

            Action.TREAT -> when {
                p == null -> ""
                p.treated -> "Уже обработано."
                s.season != Season.AUTUMN || s.dom < 25 -> "Обработка — с 25 дня осени."
                !s.spend(Action.TREAT.energy) -> ""
                else -> {
                    p.treated = true
                    "Обработано от грибковых."
                }
            }

            Action.WHITEWASH -> when {
                p == null -> ""
                p.whitewashed -> "Уже побелено."
                s.season != Season.AUTUMN || s.dom < 25 -> "Побелка — с 25 дня осени."
                !s.spend(Action.WHITEWASH.energy) -> ""
                else -> {
                    p.whitewashed = true
                    "Побелено."
                }
            }

            Action.WINTER_COVER -> when {
                p == null -> ""
                p.winterCover -> "Уже укрыто на зиму."
                s.season != Season.AUTUMN -> "Укрытие на зиму — осенью."
                !s.spend(Action.WINTER_COVER.energy) -> ""
                else -> {
                    p.winterCover = true
                    "Укрыто на зиму."
                }
            }

            Action.SNOW_CLEAR -> when {
                p == null -> ""
                p.snowCleared -> "Уже откопано."
                s.season != Season.WINTER -> "Откапывают снег зимой."
                !s.spend(Action.SNOW_CLEAR.energy) -> ""
                else -> {
                    p.snowCleared = true
                    "Снег откопан от корней."
                }
            }
        }

        s.say(message)
    }

    fun buy(s: GameState, c: CropDef) {
        val price = if (s.buildings["compost"] == true) (c.price * 0.85).roundToInt() else c.price
        if (s.money < price) {
            s.say("Не хватает денег.")
            return
        }
        s.money -= price
        s.seedlings[c.id] = (s.seedlings[c.id] ?: 0) + 1
        s.say("Куплено: ${c.name} за $price ₽.")
    }

    fun buyBuilding(s: GameState, b: Building) {
        if (s.buildings[b.id] == true) {
            s.say("Уже построено.")
            return
        }
        if (s.money < b.price) {
            s.say("Не хватает денег.")
            return
        }
        s.money -= b.price
        s.buildings[b.id] = true
        s.say("Построено: ${b.name}.")
    }

    fun sellAll(s: GameState) {
        if (s.storage.isEmpty()) {
            s.say("Склад пуст.")
            return
        }
        var total = 0.0
        var kgAll = 0.0
        s.storage.forEach { (id, kg) ->
            if (kg > 0.01) {
                total += kg * marketPrice(s, id)
                kgAll += kg
                s.sold[id] = (s.sold[id] ?: 0.0) + kg
            }
        }
        s.storage.clear()
        s.money += total.roundToInt()
        s.say("Продано " + "%.1f".format(kgAll) + " кг за " + total.roundToInt() + " ₽.")
    }

    fun clearFind(s: GameState) {
        val i = s.tiles.indexOfFirst { it.blocked }
        if (i < 0) {
            s.say("Открытых находок нет.")
            return
        }
        s.tiles[i].blocked = false
        s.skillKnowledge++
        s.say("Находка оформлена. Место свободно.")
    }

    fun restoreArtifact(s: GameState) {
        if (s.season != Season.WINTER) {
            s.say("Реставрация — зимой.")
            return
        }
        if (s.artifacts.isEmpty()) {
            s.say("Нет находок.")
            return
        }
        if (!s.spend(2)) return
        s.restorations++
        s.skillKnowledge++
        s.money += 300
        s.say("Отреставрировано. Знание +1, +300 ₽.")
    }

    fun readArchive(s: GameState) {
        if (s.season != Season.WINTER) {
            s.say("Архив — зимой.")
            return
        }
        val era = Eras.all[s.eraIndex.coerceIn(0, Eras.all.lastIndex)]
        if (s.notes.contains(era.note)) {
            s.say("Эта запись уже в дневнике.")
            return
        }
        if (!s.spend(1)) return
        s.archiveRead++
        s.notes.add(era.note)
        s.say("Из архива: ${era.name}.")
    }

    fun nextDay(s: GameState) {
        if (s.frostDay == s.day) applyFrost(s)

        val winter = s.season == Season.WINTER

        s.plantsWithTiles().forEach { (_, p) ->
            if (!p.alive) return@forEach
            val c = Crops.byId(p.cropId)

            if (p.ripeKg > 0.0 && s.day - p.ripeDay > 3) {
                s.say("${c.name}: урожай пропал — не убрали вовремя.")
                p.ripeKg = 0.0
                p.ripeDay = -1
            }

            if (winter) return@forEach

            if (s.day - p.lastWatered >= c.thirstDays) {
                p.alive = false
                s.say("${c.name}: погибло от засухи.")
            }

            if (p.covered && p.uncoverDay in 0..s.day) {
                p.covered = false
                p.uncoverDay = -1
                p.frostLoss = maxOf(p.frostLoss, 0.25f)
                s.say("${c.name}: укрытие не сняли вовремя.")
            }
        }

        s.day++
        s.energy = 100

        if (s.day >= YEAR_DAYS) {
            finishYear(s)
            return
        }

        if (s.season == Season.SPRING && s.dom in 5..24 && s.frostDay < 0 && Random.nextInt(100) < 20) {
            s.frostDay = s.day + 3
            s.say("ТВ: заморозок на ${domOf(s.frostDay)}-й день весны.")
        }

        if (s.season == Season.AUTUMN && s.dom == 25 && !s.winterKnown) {
            s.winterKnown = true
            s.winter = WinterWeather.values().random()
            s.say("ТВ: зима ожидается ${s.winter.label}.")
        }

        if (s.season == Season.WINTER) return

        s.plantsWithTiles().forEach { (_, p) ->
            if (!p.alive || p.ripeKg > 0.0) return@forEach
            val c = Crops.byId(p.cropId)
            if (p.age < c.firstYear) return@forEach
            if (!c.harvestDays.contains(s.day)) return@forEach

            val idx = (p.age - c.firstYear).coerceIn(0, c.curve.lastIndex)
            var mult = c.curve[idx] / 100.0
            if (p.nitrogen) mult *= 1.2
            if (p.pk) mult *= 1.2
            mult *= (1.0 - p.disease)
            mult *= (1.0 - p.frostLoss)

            if (!pollinated(s, p)) {
                mult *= 0.3
                s.say("${c.name}: нет второго сорта-опылителя — урожай мал.")
            }

            val kg = c.maxKg * mult
            if (kg > 0.01) {
                p.ripeKg = kg
                p.ripeDay = s.day
                s.say("${c.name}: поспело " + "%.1f".format(kg) + " кг.")
            }
        }
    }

    private fun applyFrost(s: GameState) {
        var dead = 0
        s.plantsWithTiles().forEach { (_, p) ->
            if (!p.alive) return@forEach
            val c = Crops.byId(p.cropId)
            if (s.buildings["greenhouse"] == true) return@forEach
            if (p.covered) {
                p.uncoverDay = s.day + 3
                return@forEach
            }
            when (Random.nextInt(100)) {
                in 0..4 -> {
                    p.alive = false
                    dead++
                }
                in 5..14 -> p.frostLoss = 1f
                in 15..34 -> p.frostLoss = maxOf(p.frostLoss, 0.5f)
                in 35..74 -> p.frostLoss = maxOf(p.frostLoss, 0.25f)
            }
        }
        s.say("Ночью ударил заморозок." + if (dead > 0) " Погибло: $dead." else "")
        s.frostDay = -1
    }

    private fun finishYear(s: GameState) {
        var lost = 0

        s.plantsWithTiles().forEach { (_, p) ->
            if (!p.alive) return@forEach
            val c = Crops.byId(p.cropId)

            if (!p.treated) {
                p.disease = minOf(0.5f, p.disease + 0.15f)
                s.say("${c.name}: без осенней обработки −15% к урожаю.")
            }

            if (c.tree && !p.whitewashed && Random.nextInt(100) < 20) {
                p.alive = false
                lost++
            }

            if (!p.winterCover && s.winter == WinterWeather.COLD && c.winterDeath > 0) {
                if (Random.nextInt(100) < c.winterDeath) {
                    p.alive = false
                    lost++
                }
            }

            if (s.winter == WinterWeather.COLD && c.winterPenalty > 0) {
                p.disease = minOf(0.5f, p.disease + c.winterPenalty / 100f)
            }

            if (s.winter == WinterWeather.SNOWY && c.snowRot > 0 && !p.snowCleared && !p.winterCover) {
                if (Random.nextInt(100) < c.snowRot) {
                    p.alive = false
                    lost++
                    s.say("${c.name}: выпрело под снегом.")
                }
            }

            p.age++
            p.ripeKg = 0.0
            p.ripeDay = -1
            p.covered = false
            p.uncoverDay = -1
            p.frostLoss = 0f
            p.nitrogen = false
            p.pk = false
            p.treated = false
            p.whitewashed = false
            p.winterCover = false
            p.snowCleared = false
        }

        s.tiles.forEach { t ->
            val p = t.plant
            if (p != null && !p.alive) {
                val def = Crops.byId(p.cropId)
                if (def.size > 1) {
                    for (cell in footprint(t.partOf, def.size)) s.tiles[cell].partOf = -1
                }
                t.plant = null
            }
        }

        s.money -= RENT
        s.sold.clear()
        s.year++
        s.day = 0
        s.energy = 100
        s.winterKnown = false
        s.frostDay = -1
        s.say("Год ${s.year}. Аренда: −$RENT ₽." + if (lost > 0) " Погибло за зиму: $lost." else "")
    }

    fun startExpedition(s: GameState) {
        if (s.energy < EXPEDITION_COST) {
            s.say("На спуск нужно $EXPEDITION_COST ⚡.")
            return
        }
        s.energy -= EXPEDITION_COST
        s.inExpedition = true
        s.light = 6
        s.context = 100
        spawn(s)
    }

    private fun spawn(s: GameState) {
        val era = Eras.all[s.eraIndex.coerceIn(0, Eras.all.lastIndex)]
        s.enemyName = era.enemies.random()
        s.enemyMax = 8 + s.eraLevel * 3
        s.enemyHp = s.enemyMax
        s.enemyWeak = Random.nextInt(3)
    }

    fun attack(s: GameState, stat: Int) {
        if (!s.inExpedition) return

        if (s.energy < ATTACK_COST) {
            s.inExpedition = false
            s.say("Силы кончились — возвращайтесь наверх.")
            return
        }
        if (s.light <= 0) {
            s.inExpedition = false
            s.say("Свет закончился — возвращаемся наверх.")
            return
        }

        s.energy -= ATTACK_COST

        val skill = when (stat) {
            0 -> s.skillForce
            1 -> s.skillKnowledge
            else -> s.skillPatience
        }

        var damage = 1 + skill
        if (stat == s.enemyWeak) {
            damage *= 2
            s.say("Верный подход. Урон $damage.")
        } else {
            damage = maxOf(1, damage / 2)
            s.say("Не тот подход. Урон $damage.")
        }

        s.enemyHp -= damage
        s.light--

        if (s.enemyHp <= 0) {
            clearLevel(s)
            return
        }

        val back = 6 + s.eraLevel
        s.context -= back
        s.say("${s.enemyName}: ответ на $back.")

        if (s.context <= 0) {
            s.context = 100
            s.inExpedition = false
            s.say("Контекст утрачен. Пласт начнётся заново.")
            return
        }

        if (s.light <= 0) {
            s.inExpedition = false
            s.say("Свет закончился — возвращаемся наверх.")
        }
    }

    private fun clearLevel(s: GameState) {
        s.skillKnowledge++
        s.say("Пласт ${s.eraLevel} пройден.")

        if (s.eraLevel >= 20) {
            val era = Eras.all[s.eraIndex.coerceIn(0, Eras.all.lastIndex)]
            if (!s.notes.contains(era.note)) s.notes.add(era.note)
            s.say("Эпоха изучена: ${era.name}.")
            s.money += 2000
            s.eraIndex = (s.eraIndex + 1).coerceAtMost(Eras.all.lastIndex)
            s.eraLevel = 1
            s.inExpedition = false
        } else {
            s.eraLevel++
            s.light = 6
            s.context = 100
            spawn(s)
        }
    }
}
