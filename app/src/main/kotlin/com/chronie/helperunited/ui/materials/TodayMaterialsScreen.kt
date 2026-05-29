package com.chronie.helperunited.ui.materials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Today's Materials screen.
 * Shows which talent/weapon upgrade materials are available today.
 * Ported from PizzaHelperUnited's GITodayMaterialsKit.
 */
@Composable
fun TodayMaterialsScreen(
    viewModel: TodayMaterialsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "📅 今日素材",
                style = MiuixTheme.textStyles.headline2,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (uiState.isSunday) "周日：全部开放" else dayName(uiState.dayOfWeek),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.primary,
            )
        }

        if (uiState.isSunday) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                pressFeedbackType = PressFeedbackType.Sink,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎉 周日所有素材副本全部开放！",
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "今天可以刷取任何天赋突破素材和武器突破素材",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }

        // Talent Materials
        SectionHeader("📖 天赋突破素材")
        uiState.talentDomains.forEach { domain ->
            MaterialDomainCard(domain)
        }

        // Weapon Materials
        SectionHeader("⚔️ 武器突破素材")
        uiState.weaponDomains.forEach { domain ->
            MaterialDomainCard(domain)
        }

        // Tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 刷素材小贴士",
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 天赋素材和武器素材在周一/周四、周二/周五、周三/周六轮换",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = "• 周日所有副本素材均可掉落",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = "• 使用「参量质变仪」可以额外获得素材",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MiuixTheme.textStyles.title2,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun MaterialDomainCard(domain: MaterialDomain) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = domain.name,
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = domain.region,
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            domain.materials.forEach { material ->
                Text(
                    text = "• $material",
                    style = MiuixTheme.textStyles.body2,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }
}

private fun dayName(dayOfWeek: Int): String = when (dayOfWeek) {
    java.util.Calendar.MONDAY -> "周一"
    java.util.Calendar.TUESDAY -> "周二"
    java.util.Calendar.WEDNESDAY -> "周三"
    java.util.Calendar.THURSDAY -> "周四"
    java.util.Calendar.FRIDAY -> "周五"
    java.util.Calendar.SATURDAY -> "周六"
    java.util.Calendar.SUNDAY -> "周日"
    else -> ""
}
