package org.app.config.domain.image.internal;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import lombok.extern.slf4j.*;
import org.app.config.domain.image.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ImageBlurerImpl implements ImageBlurer {

    private static final int DESIRED_DOWNSCALE_IMG_SIZE = 64;

    private static final float[] GAUSSIAN_FILTER_3X3 = {
            1f / 16f, 2f / 16f, 1f / 16f,
            2f / 16f, 4f / 16f, 2f / 16f,
            1f / 16f, 2f / 16f, 1f / 16f
    };

    private static final ConvolveOp CONVLUTION = new ConvolveOp(
            new Kernel(3, 3, GAUSSIAN_FILTER_3X3),
            ConvolveOp.EDGE_NO_OP, null
    );
    private static final String BLUR_IMG_FORMAT = "png";

    private static BufferedImage resizeImage(
            BufferedImage image, int newWidth, int newHeight
    ) {
        BufferedImage dst = new BufferedImage(
                newWidth, newHeight, BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = dst.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        graphics.drawImage(image, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        return dst;
    }

    private static FailedToCreateBlurImageException logErrAndGetEx(
            String message, Throwable cause
    ) {
        log.warn(message, cause);
        return new FailedToCreateBlurImageException(message, cause);
    }

    @Override
    public byte[] blurImage(byte[] image) throws FailedToCreateBlurImageException {

        try (
                ByteArrayInputStream in = new ByteArrayInputStream(image);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {

            BufferedImage source;

            if ((source = ImageIO.read(in)) == null) {
                throw logErrAndGetEx(
                        "Failed to get BufferedImage (null given)",
                        null
                );
            }

            int originHeight = source.getHeight();
            int originWidth = source.getWidth();

            int ratio = Math.max(
                    1,
                    Math.min(
                            originHeight / DESIRED_DOWNSCALE_IMG_SIZE,
                            originWidth / DESIRED_DOWNSCALE_IMG_SIZE
                    )
            );

            int downHeight = originHeight / ratio;
            int downWidth = originWidth / ratio;

            log.info("Downscaling image with ratio: {}", ratio);
            BufferedImage downscaledImage = resizeImage(source, downWidth, downHeight);

            BufferedImage blurredImage = CONVLUTION.filter(downscaledImage, null);

            log.info("Upscaling image with ratio: {}", ratio);
            BufferedImage upscaledImage = resizeImage(blurredImage, originWidth, originHeight);

            if (!ImageIO.write(upscaledImage, BLUR_IMG_FORMAT, out)) {
                throw logErrAndGetEx(
                        "Failed to write blur image (no appropriate writer found)",
                        null
                );
            }

            return out.toByteArray();

        } catch (IOException e) {
            String errMsg = String.format(
                    "Failed to blur image due to ex: %s",
                    e.getClass().getSimpleName()
            );
            throw logErrAndGetEx(errMsg, e);
        }
    }
}
