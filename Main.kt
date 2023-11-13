import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    var xMin by remember { mutableStateOf(-10) }
    var yMin by remember { mutableStateOf(0) }
    var xMax by remember { mutableStateOf( 10) }
    var yMinMax by remember{mutableStateOf(0f)}
    var selectY by remember{mutableStateOf(true)}
    val textMeasurer = rememberTextMeasurer()

    var points by remember { mutableStateOf(mutableMapOf<Float, Float>()) }
    Canvas(modifier = Modifier.fillMaxSize().clickable{}.
    onPointerEvent(PointerEventType.Press){
        var point = it.changes.first().position
        points[point.x] = point.y
    },
        onDraw = {
            var yMax = this.size.height*(xMax-xMin)/this.size.width+yMin
            if(yMax*yMin<0){
                yMinMax = ((xMax-xMin)/(xMax+xMin)).toFloat()
            }
            drawLine(
                color = Color.Black,
                start = Offset(0f, this.size.height * (1 + yMinMax) / 2),
                end = Offset(this.size.width, this.size.height * (1 + yMinMax) / 2)
            )
            val x = FloatArray(points.size)
            val y = FloatArray(points.size)
            drawLine(color = Color.Black,
                start = Offset(-this.size.width*xMin/(xMax-xMin), 0f),
                end = Offset(-this.size.width*xMin/(xMax-xMin), this.size.height))
            var count = 0
            for(point in points) {
                x[count] = point.key
                y[count] = point.value
                drawCircle(
                    color = Color.Green,
                    radius = 10f,
                    center = Offset(point.key, point.value)
                )
                count++
            }

            var xvals = IntArray(this.size.width.toInt())
            for (i in 0..this.size.width.toInt()){
                xvals+=i
            }
            val yvals: FloatArray = LagrangeInterpolation(x, y, xvals)

            for (i in 0..xvals.size-2){
                drawLine(color = Color.Black,
                    start = Offset(xvals[i].toFloat(),yvals[i]),
                    end = Offset(xvals[i+1].toFloat(), yvals[i+1]))
            }

            for(i in xMin .. xMax) {
                drawLine(color = Color.Black,
                    start = Offset(this.size.width*(i-xMin)/(xMax-xMin),
                        this.size.height*(1+yMinMax)/2-5),
                    end = Offset(this.size.width*(i-xMin)/(xMax-xMin),
                        this.size.height*(1+yMinMax)/2+5))
                drawText(textMeasurer = textMeasurer, text = i.toString(),
                    topLeft = Offset(this.size.width*(i-xMin)/(xMax-xMin)-5,
                        this.size.height*(1+yMinMax)/2))
            }
        })

    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
        Row(modifier = Modifier.padding(10.dp, 10.dp)){
            Column(modifier = Modifier.padding(10.dp, 10.dp)){
                Text("xMin")
                TextField(value = xMin.toString(),
                    onValueChange = { value -> xMin = value.toIntOrNull() ?:-10 })
            }
            Column(modifier = Modifier.padding(10.dp, 10.dp)) {
                Text("xMax")
                TextField(value = xMax.toString(),
                    onValueChange = { value -> xMax = value.toIntOrNull() ?: 0 })
            }
            Column{
                Row{
                    RadioButton(
                        selected = selectY,
                        onClick = { selectY = true },
                        modifier = Modifier.padding(8.dp)
                    )
                    Text("yMin", fontSize = 22.sp)
                }
                Row{
                    RadioButton(
                        selected = !selectY,
                        onClick = { selectY = false },
                    )
                    Text("ySlider", fontSize = 22.sp)
                }
            }
            Column(modifier = Modifier.padding(10.dp, 10.dp)) {
                if(!selectY) {
                    Slider(value = yMinMax,
                        valueRange = -1f..1f,
                        steps = 9,
                        onValueChange = { yMinMax = it })
                }
                else{
                    TextField(value = xMin.toString(),
                        onValueChange = { value -> yMin = value.toIntOrNull() ?: 0 })
                }
            }
        }
    }
}

fun LagrangeInterpolation(x: FloatArray, y: FloatArray, xval: Float): Float {
    var yval = 0f
    var Products: Float
    for (i in x.indices) {
        Products = y[i]
        for (j in x.indices) {
            if (i != j) {
                Products *= (xval - x[j]) / (x[i] - x[j])
            }
        }
        yval += Products
    }
    return yval
}

fun LagrangeInterpolation(x: FloatArray, y: FloatArray, xvals: IntArray): FloatArray {
    val yvals = FloatArray(xvals.size)
    for (i in xvals.indices) yvals[i] = LagrangeInterpolation(x, y, xvals[i].toFloat())
    return yvals
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}