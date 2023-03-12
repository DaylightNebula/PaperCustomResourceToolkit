package daylightnebula.papercustomresourcetoolkit.packer

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Base64
import javax.imageio.ImageIO

object TextureAllocator {
    private var curTextureID = 0

    fun saveTexture(texString: String): Int {
        // get image bytes by decoding base 64, skipping the header
        val imageBytes = Base64.getDecoder().decode(texString.substring("data:image/png;base64,".length))

        // use ImageIO to read the buffered image from the bytes gathered above
        return saveTexture(ImageIO.read(ByteArrayInputStream(imageBytes)))
    }

    fun saveTexture(file: File): Int {
        // get a new texture id
        val texID = curTextureID++

        // get target file
        val target = File(ResourcePack.texturesFolder, "$texID.png")

        // copy file to the target
        file.copyTo(target, overwrite = true)

        // return texture id
        return texID
    }

    private fun saveTexture(image: BufferedImage): Int {
        // get a new texture id
        val texID = curTextureID++

        // get target file
        val file = File(ResourcePack.texturesFolder, "$texID.png")

        // write image to the target file using ImageIO
        ImageIO.write(image, "png", file)

        // return texture id
        return texID
    }
}