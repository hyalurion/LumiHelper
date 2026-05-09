package com.chronie.helperunited.ui.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for Today's Materials screen.
 * Shows which talent/weapon materials are available today.
 * Ported from PizzaHelperUnited's GITodayMaterialsKit.
 */
@HiltViewModel
class TodayMaterialsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TodayMaterialsUiState())
    val uiState: StateFlow<TodayMaterialsUiState> = _uiState.asStateFlow()

    init {
        loadMaterials()
    }

    private fun loadMaterials() {
        viewModelScope.launch {
            val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val todayIndex = when (dayOfWeek) {
                Calendar.MONDAY, Calendar.THURSDAY -> 0
                Calendar.TUESDAY, Calendar.FRIDAY -> 1
                Calendar.WEDNESDAY, Calendar.SATURDAY -> 2
                Calendar.SUNDAY -> -1 // All available
                else -> 0
            }

            _uiState.value = TodayMaterialsUiState(
                dayOfWeek = dayOfWeek,
                todayIndex = todayIndex,
                isSunday = todayIndex == -1,
            )
        }
    }
}

data class TodayMaterialsUiState(
    val dayOfWeek: Int = Calendar.MONDAY,
    val todayIndex: Int = 0,
    val isSunday: Boolean = false,
) {
    /**
     * GI talent book domains and their materials.
     * M/W/F and T/T/S/S rotations.
     */
    val talentDomains: List<MaterialDomain>
        get() {
            val all = listOf(
                // Monday/Thursday
                MaterialDomain(
                    name = "忘却之峡",
                    region = "蒙德",
                    materials = listOf("自由", "抗争", "诗文"),
                    days = listOf(0, 3), // Mon, Thu
                ),
                // Tuesday/Friday
                MaterialDomain(
                    name = "太山府",
                    region = "璃月",
                    materials = listOf("繁荣", "勤劳", "黄金"),
                    days = listOf(1, 4), // Tue, Fri
                ),
                // Wednesday/Saturday
                MaterialDomain(
                    name = "菫色之庭",
                    region = "稻妻",
                    materials = listOf("浮世", "风雅", "天光"),
                    days = listOf(2, 5), // Wed, Sat
                ),
                // Monday/Thursday
                MaterialDomain(
                    name = "昏识塔",
                    region = "须弥",
                    materials = listOf("诤言", "巧思", "笃行"),
                    days = listOf(0, 3),
                ),
                // Tuesday/Friday
                MaterialDomain(
                    name = "苍白的遗荣",
                    region = "枫丹",
                    materials = listOf("公平", "正义", "秩序"),
                    days = listOf(1, 4),
                ),
                // Wednesday/Saturday
                MaterialDomain(
                    name = "蕴火之渊",
                    region = "纳塔",
                    materials = listOf("焚燔", "角逐", "纷争"),
                    days = listOf(2, 5),
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
                ),
                MaterialDomain(
                    name = "震雷连山密宫",
                    region = "璃月",
                    materials = listOf("孤云寒林", "雾海云间", "漆黑陨铁"),
                    days = listOf(1, 4),
                ),
                MaterialDomain(
                    name = "砂流之庭",
                    region = "稻妻",
                    materials = listOf("远海夷地", "鸣神御灵", "今昔剧画"),
                    days = listOf(2, 5),
                ),
                MaterialDomain(
                    name = "有顶塔",
                    region = "须弥",
                    materials = listOf("谧林涓露", "绿洲花园", "烈日威权"),
                    days = listOf(0, 3),
                ),
                MaterialDomain(
                    name = "深潮的余响",
                    region = "枫丹",
                    materials = listOf("纯圣露滴", "悠古弦音", "无垢之海"),
                    days = listOf(1, 4),
                ),
                MaterialDomain(
                    name = "凝燃之山",
                    region = "纳塔",
                    materials = listOf("谵妄", "神合", "裂阳"),
                    days = listOf(2, 5),
                ),
            )
            return if (isSunday) all else all.filter { it.days.contains(todayIndex) }
        }
}

data class MaterialDomain(
    val name: String,
    val region: String,
    val materials: List<String>,
    val days: List<Int>, // 0=Mon, 1=Tue, ..., 5=Sat
)
