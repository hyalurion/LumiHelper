package com.chronie.helperunited.ui.gacha.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chronie.helperunited.model.BarChartEntry
import com.chronie.helperunited.model.FiveStarRecord
import com.chronie.helperunited.model.GachaTypeAnalytics
import com.chronie.helperunited.model.LineChartPoint
import com.chronie.helperunited.model.PieChartSlice
import com.chronie.helperunited.model.PityTracker
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Gacha analytics screen with Canvas-based charts.
 * Shows per-gacha-type tabs and visual analytics.
 */
@Composable
fun GachaAnalyticsScreen(
    viewModel: GachaAnalyticsViewModel,
) {
    val state by viewModel.uiState.collectAsState()

    GachaAnalyticsContent(
        state = state,
        onTypeSelect = { viewModel.selectType(it) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GachaAnalyticsContent(
    state: GachaAnalyticsUiState,
    onTypeSelect: (Int) -> Unit,
) {
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    pressFeedbackType = PressFeedbackType.Sink,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "提示",
                            style = MiuixTheme.textStyles.title2,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.error, style = MiuixTheme.textStyles.body2)
                    }
                }
            }
        }
        state.allAnalytics.isNotEmpty() -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Gacha type tabs
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    GachaTypeTabs(
                        analytics = state.allAnalytics,
                        selectedIndex = state.selectedTypeIndex,
                        onSelect = onTypeSelect,
                    )
                }

                val selected = state.selectedAnalytics ?: return@LazyColumn

                // Summary cards
                item {
                    SummaryCards(selected)
                }

                // Pity display
                item {
                    PityCard(selected.pityTracker)
                }

                // Rarity distribution (Donut chart)
                item {
                    DonutChartCard(selected.rarityDistribution, selected.totalPulls)
                }

                // Cumulative 5★ line chart
                if (selected.cumulativeFiveStar.isNotEmpty()) {
                    item {
                        LineChartCard(selected.cumulativeFiveStar)
                    }
                }

                // Bar chart (pulls per banner)
                if (selected.bannerPulls.isNotEmpty()) {
                    item {
                        BarChartCard(selected.bannerPulls)
                    }
                }

                // 5★ history
                if (selected.pityTracker.fiveStarHistory.isNotEmpty()) {
                    item {
                        Text(
                            text = "★5 历史记录",
                            style = MiuixTheme.textStyles.title2,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    items(selected.pityTracker.fiveStarHistory.reversed()) { record ->
                        FiveStarHistoryCard(record)
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ========== Gacha Type Tabs ==========

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GachaTypeTabs(
    analytics: List<GachaTypeAnalytics>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        analytics.forEachIndexed { index, item ->
            Button(onClick = { onSelect(index) }) {
                Text(
                    text = "${item.gachaTypeName} (${item.totalPulls})",
                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

// ========== Summary Cards ==========

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryCards(analytics: GachaTypeAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 ${analytics.gachaTypeName} 统计",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetricItem("总抽数", "${analytics.totalPulls}")
                MetricItem("★★★★★", "${analytics.fiveStarCount}")
                MetricItem("★★★★", "${analytics.fourStarCount}")
                MetricItem("★★★", "${analytics.threeStarCount}")
                if (analytics.totalPulls > 0) {
                    MetricItem("五星率", "${"%.1f".format(analytics.fiveStarRate * 100)}%")
                    MetricItem("四星率", "${"%.1f".format(analytics.fourStarRate * 100)}%")
                }
                if (analytics.pityTracker.averagePity > 0) {
                    MetricItem("平均出金", "${"%.1f".format(analytics.pityTracker.averagePity)}抽")
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MiuixTheme.textStyles.title1,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MiuixTheme.textStyles.main,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

// ========== Pity Card ==========

@Composable
private fun PityCard(pity: PityTracker) {
    val progress = pity.currentPity.toFloat() / pity.hardPity
    val progressColor = when {
        pity.currentPity >= pity.hardPity -> Color(0xFFFF5252) // Red - past hard pity
        pity.currentPity >= 70 -> Color(0xFFFFA726) // Orange - soft pity zone
        pity.currentPity >= 50 -> Color(0xFFFFCA28) // Yellow
        else -> Color(0xFF66BB6A) // Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🎯 保底计数",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${pity.currentPity} / ${pity.hardPity}",
                    style = MiuixTheme.textStyles.headline2,
                    fontWeight = FontWeight.Bold,
                    color = progressColor,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
            ) {
                // Background
                drawRoundRect(
                    color = Color(0xFFE0E0E0),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                    size = Size(size.width, size.height),
                )
                // Progress
                drawRoundRect(
                    color = progressColor,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                    size = Size(size.width * progress.coerceIn(0f, 1f), size.height),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "剩余 ${pity.hardPity - pity.currentPity} 抽保底",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = "最长无金: ${pity.maxPity} 抽",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }

            if (pity.isGuaranteed) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ 已接近硬保底！",
                    style = MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252),
                )
            }
        }
    }
}

// ========== Donut Chart ==========

@Composable
private fun DonutChartCard(slices: List<PieChartSlice>, totalPulls: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 稀有度分布",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Donut chart
                Canvas(
                    modifier = Modifier.size(140.dp),
                ) {
                    val strokeWidth = 28.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    var startAngle = -90f
                    for (slice in slices) {
                        val sweepAngle = if (totalPulls > 0) {
                            slice.value / totalPulls * 360f
                        } else {
                            0f
                        }
                        drawArc(
                            color = Color(slice.color),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                        )
                        startAngle += sweepAngle
                    }

                    // Center text
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        paint.textSize = 14.dp.toPx()
                        paint.color = android.graphics.Color.BLACK
                        drawText("$totalPulls", center.x, center.y + 5.dp.toPx(), paint)
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    slices.forEach { slice ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(slice.color), CircleShape),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${slice.label}: ${slice.value.toInt()} (${"%.1f".format(slice.percentage)}%)",
                                style = MiuixTheme.textStyles.main,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ========== Line Chart (Cumulative 5★) ==========

@Composable
private fun LineChartCard(points: List<LineChartPoint>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📈 ★5 累计趋势",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            ) {
                if (points.isEmpty()) return@Canvas

                val maxY = points.maxOf { it.y }
                val padding = 16.dp.toPx()
                val chartWidth = size.width - padding * 2
                val chartHeight = size.height - padding * 2

                // Grid lines
                val gridColor = Color(0xFFE0E0E0)
                for (i in 0..4) {
                    val y = padding + chartHeight * (1 - i / 4f)
                    drawLine(
                        color = gridColor,
                        start = Offset(padding, y),
                        end = Offset(size.width - padding, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                    // Y-axis labels
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            textSize = 10.dp.toPx()
                            color = android.graphics.Color.GRAY
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                        val label = "${(maxY * i / 4).toInt()}"
                        drawText(label, padding - 4.dp.toPx(), y + 4.dp.toPx(), paint)
                    }
                }

                // Line path
                val path = Path()
                val lineColor = Color(0xFFFFD700)

                points.forEachIndexed { index, point ->
                    val x = padding + point.x * chartWidth
                    val y = padding + chartHeight * (1 - point.y / maxY)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }

                    // Draw point dot
                    drawCircle(
                        color = lineColor,
                        radius = 3.dp.toPx(),
                        center = Offset(x, y),
                    )
                }

                // Draw line
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "共 ${points.size} 个 ★5 角色/物品",
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

// ========== Bar Chart (Pulls per Banner) ==========

@Composable
private fun BarChartCard(entries: List<BarChartEntry>) {
    val maxVal = entries.maxOfOrNull { it.value } ?: 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 各期抽数分布",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            ) {
                if (entries.isEmpty()) return@Canvas

                val padding = 16.dp.toPx()
                val chartWidth = size.width - padding * 2
                val chartHeight = size.height - padding * 2
                val barCount = entries.size
                val barSpacing = 4.dp.toPx()
                val barWidth = (chartWidth - barSpacing * (barCount - 1)) / barCount

                entries.forEachIndexed { index, entry ->
                    val barHeight = (entry.value / maxVal) * chartHeight
                    val x = padding + index * (barWidth + barSpacing)
                    val y = padding + chartHeight - barHeight

                    // Bar
                    drawRect(
                        color = Color(entry.color),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                    )

                    // Value label on top
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            textSize = 9.dp.toPx()
                            color = android.graphics.Color.DKGRAY
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(
                            "${entry.value.toInt()}",
                            x + barWidth / 2,
                            y - 4.dp.toPx(),
                            paint,
                        )
                    }

                    // X-axis label (rotated)
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            textSize = 8.dp.toPx()
                            color = android.graphics.Color.GRAY
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        val labelText = entry.label.take(8)
                        drawText(
                            labelText,
                            x + barWidth / 2,
                            size.height - 2.dp.toPx(),
                            paint,
                        )
                    }
                }
            }
        }
    }
}

// ========== 5★ History Card ==========

@Composable
private fun FiveStarHistoryCard(record: FiveStarRecord) {
    val pityColor = when {
        record.pityCount >= 80 -> Color(0xFFFF5252) // Red
        record.pityCount >= 70 -> Color(0xFFFFA726) // Orange
        record.pityCount >= 60 -> Color(0xFFFFCA28) // Yellow
        else -> Color(0xFF66BB6A) // Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Pity count badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(pityColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${record.pityCount}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MiuixTheme.textStyles.title3,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.name ?: "未知 ★5",
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                )
                Text(
                    text = record.time,
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }

            Text(
                text = "★★★★★",
                style = MiuixTheme.textStyles.main,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
