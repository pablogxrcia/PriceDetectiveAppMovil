package dam.moviles.app_pricedetective.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import dam.moviles.app_pricedetective.data.model.Product
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator(private val context: Context) {

    private fun generarCodigoBarras(ean: String): Bitmap? {
        return try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix: BitMatrix = multiFormatWriter.encode(
                ean,
                BarcodeFormat.EAN_13,
                100, // Reducido aún más para que quepa en la columna
                30   // Reducido aún más para que quepa en la columna
            )
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun dividirTextoEnLineas(texto: String, maxCaracteres: Int): List<String> {
        val palabras = texto.split(" ")
        val lineas = mutableListOf<String>()
        var lineaActual = ""

        for (palabra in palabras) {
            if (lineaActual.length + palabra.length + 1 <= maxCaracteres) {
                lineaActual += if (lineaActual.isEmpty()) palabra else " $palabra"
            } else {
                lineas.add(lineaActual)
                lineaActual = palabra
            }
        }
        if (lineaActual.isNotEmpty()) {
            lineas.add(lineaActual)
        }
        return lineas
    }

    fun generarListaCompra(productos: List<Product>): File {
        val pdfDocument = PrintedPdfDocument(
            context,
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
        )

        val pageHeight = pdfDocument.pageHeight.toFloat()
        var currentPage = 0
        var page = pdfDocument.startPage(currentPage)
        var canvas = page.canvas
        val paint = android.graphics.Paint()
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        // Configurar el estilo del texto
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 12f

        // Título
        paint.textSize = 20f
        canvas.drawText("Lista de la Compra", 50f, 50f, paint)
        
        // Fecha
        paint.textSize = 12f
        canvas.drawText("Fecha: $fecha", 50f, 80f, paint)

        // Agrupar productos por tienda
        val productosPorTienda = productos.groupBy { it.stores ?: "Sin tienda" }

        var yPosition = 120f
        var totalGeneral = 0.0

        // Iterar sobre cada tienda
        productosPorTienda.forEach { (tienda, productosTienda) ->
            // Verificar si necesitamos una nueva página
            if (yPosition > pageHeight - 100) {
                pdfDocument.finishPage(page)
                currentPage++
                page = pdfDocument.startPage(currentPage)
                canvas = page.canvas
                yPosition = 50f
            }

            // Título de la tienda
            paint.textSize = 16f
            canvas.drawText(tienda, 50f, yPosition, paint)
            yPosition += 30f

            // Cabecera de la tabla
            paint.textSize = 12f
            canvas.drawText("Producto", 50f, yPosition, paint)
            canvas.drawText("Precio", 250f, yPosition, paint)
            canvas.drawText("Cant.", 350f, yPosition, paint)
            canvas.drawText("Total", 400f, yPosition, paint)
            canvas.drawText("Código", 480f, yPosition, paint)
            yPosition += 20f

            // Línea separadora
            canvas.drawLine(50f, yPosition, 520f, yPosition, paint)
            yPosition += 20f

            var totalTienda = 0.0

            // Productos de la tienda
            productosTienda.forEach { producto ->
                // Verificar si necesitamos una nueva página
                if (yPosition > pageHeight - 50) {
                    pdfDocument.finishPage(page)
                    currentPage++
                    page = pdfDocument.startPage(currentPage)
                    canvas = page.canvas
                    yPosition = 50f
                }

                val precioUnitario = producto.price ?: 0.0
                val totalProducto = precioUnitario * producto.cantidad
                totalTienda += totalProducto

                // Dividir el nombre del producto en líneas
                val lineasNombre = dividirTextoEnLineas(producto.productName, 25)
                var alturaMaxima = 0f

                // Dibujar cada línea del nombre
                lineasNombre.forEach { linea ->
                    canvas.drawText(linea, 50f, yPosition, paint)
                    yPosition += 15f
                    alturaMaxima += 15f
                }

                // Ajustar la posición Y para alinear con el resto de información
                yPosition -= alturaMaxima

                // Dibujar el resto de la información
                canvas.drawText(String.format("%.2f €", precioUnitario), 250f, yPosition, paint)
                canvas.drawText(producto.cantidad.toString(), 350f, yPosition, paint)
                canvas.drawText(String.format("%.2f €", totalProducto), 400f, yPosition, paint)

                // Generar y dibujar código de barras si existe EAN
                producto.ean?.let { ean ->
                    if (ean.isNotEmpty()) {
                        generarCodigoBarras(ean)?.let { barcodeBitmap ->
                            canvas.drawBitmap(barcodeBitmap, 480f, yPosition - 20f, paint)
                        }
                    }
                }

                // Ajustar la posición Y para el siguiente producto
                yPosition += maxOf(alturaMaxima, 30f) + 15f
            }

            // Total de la tienda
            yPosition += 10f
            paint.textSize = 14f
            canvas.drawText("Total $tienda: ${String.format("%.2f €", totalTienda)}", 400f, yPosition, paint)
            totalGeneral += totalTienda
            yPosition += 30f
        }

        // Total general
        if (yPosition > pageHeight - 50) {
            pdfDocument.finishPage(page)
            currentPage++
            page = pdfDocument.startPage(currentPage)
            canvas = page.canvas
            yPosition = 50f
        }
        
        yPosition += 20f
        paint.textSize = 16f
        canvas.drawText("Total General: ${String.format("%.2f €", totalGeneral)}", 400f, yPosition, paint)

        pdfDocument.finishPage(page)

        // Guardar el PDF
        val file = File(context.getExternalFilesDir(null), "lista_compra_${System.currentTimeMillis()}.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }
} 