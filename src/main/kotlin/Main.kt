import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sunnychung.lib.multiplatform.kdatetime.KInstant
import com.sunnychung.lib.multiplatform.kdatetime.KZoneOffset
import com.sunnychung.lib.multiplatform.kdatetime.KZonedInstant
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.DefaultPoint
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.LongLinearAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.autoScaleYRange
import io.github.koalaplot.core.xygraph.rememberAxisStyle
import java.util.SortedMap
import java.util.TreeMap

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
@Preview
fun App() {
    var latenciesMsOverTime: SortedMap<Long, SingleStatistic> by remember { mutableStateOf(TreeMap()) }

    Column {
        Row {
            Button(onClick = {
                latenciesMsOverTime = latenciesMsOverTime.toSortedMap().also {
                    it.put(
                        it.size.toLong(),
                        SingleStatistic(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                    )
                }
            }) {
                Text("Add")
            }
            Button(onClick = {
                latenciesMsOverTime = latenciesMsOverTime.toSortedMap().also { it.remove(it.lastKey()) }
            }) {
                Text("Remove")
            }
        }

        ChartLayout(title = { Text("Latencies over Time (95%)") }, modifier = Modifier.heightIn(max = 300.dp)) {
            val points = latenciesMsOverTime.map { (timestamp, result) ->
                DefaultPoint(timestamp, result.at95Percent)
            }
            val pseudoPoints = points.ifEmpty {
                val now = KInstant.now().toMilliseconds()
                listOf(DefaultPoint(now - 2000, 0.0), DefaultPoint(now, 0.0))
            }.let {
                if (it.size == 1) {
                    listOf(DefaultPoint(it.first().x - 2000, it.first().y), it.first())
                } else {
                    it
                }
            }

            XYGraph(
                xAxisModel = LongLinearAxisModel(
                    pseudoPoints.first().x..pseudoPoints.last().x,
                    minorTickCount = 0,
                    minimumMajorTickIncrement = 5000,
                ),
                yAxisModel = DoubleLinearAxisModel(
                    pseudoPoints.autoScaleYRange(),
                    minorTickCount = 2,
                ),
                xAxisLabels = { KZonedInstant(it, KZoneOffset.local()).format("HH:mm:ss") },
                yAxisLabels = { String.format("%.1f", it) },
                xAxisStyle = rememberAxisStyle(Color.Blue, 20.dp, 0.dp),
                yAxisStyle = rememberAxisStyle(Color.Blue, 20.dp, 0.dp),
                xAxisTitle = "Time",
                yAxisTitle = "Latency (ms)",
            ) {
                if (points.isNotEmpty()) {
                    if (points.size > 1) {
                        LinePlot(
                            points,
                            lineStyle = LineStyle(strokeWidth = 1.dp, brush = SolidColor(Color.Blue))
                        )
                    }
                    LinePlot(
                        points,
                        symbol = {
                            Symbol(
                                fillBrush = SolidColor(Color.Yellow),
                                outlineBrush = SolidColor(Color.Blue),
                                shape = CircleShape,
                            )
                        }
                    )
                }
            }
        }
    }
}

data class SingleStatistic(
    val min: Double,
    val max: Double,
    val average: Double,
    val median: Double,
    val at90Percent: Double,
    val at95Percent: Double,
    val at99Percent: Double,
)

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
