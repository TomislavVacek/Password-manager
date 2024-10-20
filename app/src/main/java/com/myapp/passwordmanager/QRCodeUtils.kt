package com.myapp.passwordmanager

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import javax.crypto.SecretKey

object QRCodeUtils {

    fun generateQRCode(text: String, size: Int = 500): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                size,
                size
            )
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Generiranje QR koda s enkripcijom
    fun generateEncryptedQRCode(text: String, secretKey: SecretKey): Bitmap? {
        val (encryptedText, iv) = EncryptionUtils.encrypt(text, secretKey)
        val combinedData = "$encryptedText:$iv"
        return generateQRCode(combinedData) // Šifrirani podatak generiramo u QR kodu
    }

    // Dekodiranje QR koda s dekripcijom
    fun decryptQRCodeData(encryptedData: String, secretKey: SecretKey): String {
        val (encryptedText, iv) = encryptedData.split(":")
        return EncryptionUtils.decrypt(encryptedText, iv, secretKey)
    }
}
