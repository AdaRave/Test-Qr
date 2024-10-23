package com.example.qrtest

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.qrtest.ui.theme.QrTestTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
Библиотека: https://mvnrepository.com/artifact/com.google.zxing/core/3.5.1
*/
class MainActivity : ComponentActivity() {
    val sytes = listOf(
        "https://www.youtube.com/",
        "https://habr.com/ru/flows/develop/hubs/",
        "https://mvnrepository.com/artifact/com.google.zxing/core/3.5.1",
        "https://apptractor.ru/info/articles/coroutines.html"
    )
    var index =
        mutableStateOf(0)


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QrTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = rememberQrBitmapPainter(
                                sytes[index.value],
                                size = 350.dp,
                                padding = 10.dp
                            ),
                            contentDescription = "youtube",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (index.value < sytes.size - 1)
                                    index.value++
                                else index.value = 0
                            },
                            colors = ButtonDefaults.buttonColors(androidx.compose.ui.graphics.Color.Black)
                        ) {
                            Text(text = "Тык")
                        }
                    }
                }
            }
        }
    }
}

/**
 * @param author "dev-niiaddy"
 **/
@Composable
fun rememberQrBitmapPainter(
    content: String, //ссылка
    size: Dp = 150.dp, // размер в dp
    padding: Dp = 0.dp //отступ
): BitmapPainter {

    val density = LocalDensity.current //текущая плотность экрана
    val sizePx = with(density) { size.roundToPx() } //пиксельный эквивалент размера
    val paddingPx = with(density) { padding.roundToPx() }


    var bitmap by remember(content) {
        mutableStateOf<Bitmap?>(null)
    } //переменная для хранения qr. Будет пересоздавать только при изменении

    LaunchedEffect(bitmap) { //лаунч эффект для создания (чтобы не было зависания пока вычисляется)
        if (bitmap != null) return@LaunchedEffect

        launch(Dispatchers.IO) {
            val qrCodeWriter = QRCodeWriter() //используется для кодирования Qr

            val encodeHints =
                mutableMapOf<EncodeHintType, Any?>() //карта, которая используется в создании Qr
                    .apply {
                        this[EncodeHintType.MARGIN] = paddingPx
                    }

            val bitmapMatrix = try {
                qrCodeWriter.encode(
                    content, BarcodeFormat.QR_CODE,
                    sizePx, sizePx, encodeHints
                )
            } catch (ex: WriterException) {
                null
            } //создается путем кодирования строки содержимого и указания высоты и ширины Qr

            val matrixWidth = bitmapMatrix?.width ?: sizePx //ширина
            val matrixHeight = bitmapMatrix?.height ?: sizePx //высота

            val newBitmap = Bitmap.createBitmap(
                bitmapMatrix?.width ?: sizePx,
                bitmapMatrix?.height ?: sizePx,
                Bitmap.Config.ARGB_8888, //конфигурация для наивысшего качества картинки
            )

            for (x in 0 until matrixWidth) {
                for (y in 0 until matrixHeight) { //цикл для матрицы
                    val shouldColorPixel = bitmapMatrix?.get(x, y)
                        ?: false // логическая переменная для определения нужно ли красить пиксель
                    val pixelColor =
                        if (shouldColorPixel) Color.BLACK else Color.WHITE // установка цвета

                    newBitmap.setPixel(x, y, pixelColor) //закрашивание пикселя
                }
            }

            bitmap = newBitmap // записываем получившееся значение
        }

    }

    return remember(bitmap) {
        val currentBitmap = bitmap ?: Bitmap.createBitmap(
            sizePx, sizePx,
            Bitmap.Config.ARGB_8888,
        ).apply { eraseColor(Color.TRANSPARENT) }

        BitmapPainter(currentBitmap.asImageBitmap())
    } //возврат qr кода
}
