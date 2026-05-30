package com.hyalurion.lumi.helper.ui.materials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hyalurion.lumi.helper.util.MaterialCharacterMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

/**
 * ViewModel for Today's Materials screen.
 * Shows which talent/weapon materials are available today
 * and which characters need them.
 * Ported from PizzaHelperUnited's GITodayMaterialsKit.
 */
@HiltViewModel
class TodayMaterialsViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TodayMaterialsUiState())
    val uiState: StateFlow<TodayMaterialsUiState> = _uiState.asStateFlow()

    init {
        loadMaterials()
    }

    private fun loadMaterials() {
        viewModelScope.launch {
            val context = getApplication<Application>()

            // Use server timezone (UTC+8) and account for 4:00 AM daily reset.
            // In HoYoverse games, a new day starts at 4:00 AM (UTC+8), not midnight.
            // Monday materials are available from Mon 4:00 to Tue 3:59, etc.
            val serverTimeZone = TimeZone.getTimeZone("Asia/Shanghai")
            val serverCalendar = Calendar.getInstance(serverTimeZone)

            val fourAmToday = Calendar.getInstance(serverTimeZone).apply {
                set(Calendar.HOUR_OF_DAY, 4)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val isAfter4am = serverCalendar.timeInMillis >= fourAmToday.timeInMillis

            val adjustedDayOfWeek = if (isAfter4am) {
                serverCalendar.get(Calendar.DAY_OF_WEEK)
            } else {
                serverCalendar.apply { add(Calendar.DAY_OF_YEAR, -1) }
                    .get(Calendar.DAY_OF_WEEK)
            }

            val todayIndex = when (adjustedDayOfWeek) {
                Calendar.MONDAY, Calendar.THURSDAY -> 0
                Calendar.TUESDAY, Calendar.FRIDAY -> 1
                Calendar.WEDNESDAY, Calendar.SATURDAY -> 2
                Calendar.SUNDAY -> -1 // All available
                else -> 0
            }

            val mappings = MaterialCharacterMapper.loadMappings(context)

            _uiState.value = TodayMaterialsUiState(
                dayOfWeek = adjustedDayOfWeek,
                todayIndex = todayIndex,
                isSunday = todayIndex == -1,
                characterMappings = mappings,
            )
        }
    }
}

data class TodayMaterialsUiState(
    val dayOfWeek: Int = Calendar.MONDAY,
    val todayIndex: Int = 0,
    val isSunday: Boolean = false,
    val characterMappings: com.hyalurion.lumi.helper.util.MaterialMappings? = null,
) {
    /**
     * GI talent book domains and their materials.
     * M/W/F and T/T/S/S rotations.
     */
    val talentDomains: List<MaterialDomain>
        get() {
            val all = listOf(
                MaterialDomain(
                    name = "忘却之峡",
                    region = "蒙德",
                    materials = listOf("自由", "抗争", "诗文"),
                    days = listOf(0, 3),
                    charactersForDomain = getCharactersForDomain("忘却之峡"),
                ),
                MaterialDomain(
                    name = "太山府",
                    region = "璃月",
                    materials = listOf("繁荣", "勤劳", "黄金"),
                    days = listOf(1, 4),
                    charactersForDomain = getCharactersForDomain("太山府"),
                ),
                MaterialDomain(
                    name = "菫色之庭",
                    region = "稻妻",
                    materials = listOf("浮世", "风雅", "天光"),
                    days = listOf(2, 5),
                    charactersForDomain = getCharactersForDomain("菫色之庭"),
                ),
                MaterialDomain(
                    name = "昏识塔",
                    region = "须弥",
                    materials = listOf("诤言", "巧思", "笃行"),
                    days = listOf(0, 3),
                    charactersForDomain = getCharactersForDomain("昏识塔"),
                ),
                MaterialDomain(
                    name = "苍白的遗荣",
                    region = "枫丹",
                    materials = listOf("公平", "正义", "秩序"),
                    days = listOf(1, 4),
                    charactersForDomain = getCharactersForDomain("苍白的遗荣"),
                ),
                MaterialDomain(
                    name = "蕴火之渊",
                    region = "纳塔",
                    materials = listOf("焚燔", "角逐", "纷争"),
                    days = listOf(2, 5),
                    charactersForDomain = getCharactersForDomain("蕴火之渊"),
                ),
            )
            return if (isSunday) all else all.filter { it.days.contains(todayIndex) }
        }

    /**
     * GI weapon material domains.
     */
    val weaponDomains: List<MaterialDomain>
        get() {
            val all = listOf(
                MaterialDomain(
                    name = "塞西莉亚苗圃",
                    region = "蒙德",
                    materials = listOf("高塔孤王", "凛风奔狼", "狮牙斗士"),
                    days = listOf(0, 3),
                    charactersForDomain = getCharactersForDomain("塞西莉亚苗圃"),
                ),
                MaterialDomain(
                    name = "震雷连山密宫",
                    region = "璃月",
                    materials = listOf("孤云寒林", "雾海云间", "漆黑陨铁"),
                    days = listOf(1, 4),
                    charactersForDomain = getCharactersForDomain("震雷连山密宫"),
                ),
                MaterialDomain(
                    name = "砂流之庭",
                    region = "稻妻",
                    materials = listOf("远海夷地", "鸣神御灵", "今昔剧画"),
                    days = listOf(2, 5),
                    charactersForDomain = getCharactersForDomain("砂流之庭"),
                ),
                MaterialDomain(
                    name = "有顶塔",
                    region = "须弥",
                    materials = listOf("谧林涓露", "绿洲花园", "烈日威权"),
                    days = listOf(0, 3),
                    charactersForDomain = getCharactersForDomain("有顶塔"),
                ),
                MaterialDomain(
                    name = "深潮的余响",
                    region = "枫丹",
                    materials = listOf("纯圣露滴", "悠古弦音", "无垢之海"),
                    days = listOf(1, 4),
                    charactersForDomain = getCharactersForDomain("深潮的余响"),
                ),
                MaterialDomain(
                    name = "凝燃之山",
                    region = "纳塔",
                    materials = listOf("谵妄", "神合", "裂阳"),
                    days = listOf(2, 5),
                    charactersForDomain = getCharactersForDomain("凝燃之山"),
                ),
            )
            return if (isSunday) all else all.filter { it.days.contains(todayIndex) }
        }

    private fun getCharactersForDomain(domainName: String): List<String> {
        val mappings = characterMappings ?: return emptyList()
        val domainMapping = mappings.talentDomains[domainName] ?: mappings.weaponDomains[domainName]
        return domainMapping?.allCharacters ?: emptyList()
    }
}

data class MaterialDomain(
    val name: String,
    val region: String,
    val materials: List<String>,
    val days: List<Int>,
    val charactersForDomain: List<String> = emptyList(),
)
