package org.ipcamera.config.controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.*;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoRtspController {
    private volatile boolean playing = false;
    private Thread playThread = null;
    private FFmpegFrameGrabber grabber;
    private Java2DFrameConverter converter;

    private WritableImage sharedWritableImage;
    private PixelWriter sharedPixelWriter;
    private byte[] bgraBuffer;
    private PixelFormat<ByteBuffer> fxFormat;

    public VideoRtspController() {}

    public void startPlaying(String url, ImageView imageView) {
        if (url == null || url.isEmpty()) return;

        // Остановим старое видео перед новым стартом
        stop();
        playing = true;

        playThread = new Thread(() -> {
            try {
                grabber = new FFmpegFrameGrabber(url);
                grabber.setPixelFormat(avutil.AV_PIX_FMT_BGR24);
                FFmpegLogCallback.set();
                grabber.setOption("rtsp_transport", "tcp");
                grabber.setOption("stimeout", "5000000");
                grabber.setVideoOption("color_range", "tv");
                grabber.setVideoOption("color_space", "bt709");

                try {
                    grabber.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    cleanupGrabber();
                    playing = false;
                    return;
                }

                if (grabber.getFormatContext() == null) {
                    cleanupGrabber();
                    playing = false;
                    return;
                }

                int width = grabber.getImageWidth();
                int height = grabber.getImageHeight();

                if (width <= 0 || height <= 0) {
                    //System.err.println("Невозможно получить размеры видео");
                    cleanupGrabber();
                    playing = false;
                    return;
                }

                // Инициализация JavaFX объектов один раз
                if (sharedWritableImage == null ||
                        sharedWritableImage.getWidth() != width ||
                        sharedWritableImage.getHeight() != height) {

                    sharedWritableImage = new WritableImage(width, height);
                    sharedPixelWriter = sharedWritableImage.getPixelWriter();
                    bgraBuffer = new byte[width * height * 4];
                    fxFormat = PixelFormat.getByteBgraInstance();
                }

                // Устанавливаем изображение в ImageView один раз
                Platform.runLater(() -> imageView.setImage(sharedWritableImage));

                // --- Основной цикл ---
                Frame frame;
                while (playing) {
                    frame = grabber.grabImage();
                    if (frame == null || frame.image == null || frame.image[0] == null) {
                        try { Thread.sleep(10); } catch (InterruptedException e) { break; }
                        continue;
                    }

                    // Получаем BGR данные напрямую из нативного буфера
                    ByteBuffer buffer = (ByteBuffer) frame.image[0];
                    buffer.rewind();

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int b = buffer.get() & 0xFF;
                            int g = buffer.get() & 0xFF;
                            int r = buffer.get() & 0xFF;
                            int idx = (y * width + x) * 4;
                            bgraBuffer[idx] = (byte) b;
                            bgraBuffer[idx + 1] = (byte) g;
                            bgraBuffer[idx + 2] = (byte) r;
                            bgraBuffer[idx + 3] = (byte) 255; // Alpha
                        }
                    }

                    // Обновляем WritableImage только если поток ещё играет
                    if (playing) {
                        Platform.runLater(() ->
                                sharedPixelWriter.setPixels(0, 0, width, height, fxFormat, bgraBuffer, 0, width * 4));
                    }

                    // Ограничение FPS
                    double fr = grabber.getFrameRate();
                    try {
                        if (fr > 1) {
                            Thread.sleep(Math.max(1, (long) (1000.0 / fr)));
                        } else {
                            Thread.sleep(20);
                        }
                    } catch (InterruptedException e) {
                        break;
                    }

                    // Освобождаем Frame
                    frame.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cleanupGrabber();
            }

        }, "Video-Play-Thread");

        playThread.setDaemon(true);
        playThread.start();
    }

    private void cleanupGrabber() {
        // закрываем grabber и конвертер
        try {
            if (grabber != null) {
                try {
                    grabber.stop();
                } catch (Exception ignored) {
                }
                try {
                    grabber.release();
                } catch (Exception ignored) {
                }
                try {
                    grabber.close();
                } catch (Exception ignored) {
                }
                grabber = null;
            }
        } finally {
            if (converter != null) {
                try {
                    converter.close();
                } catch (Exception ignored) {}
                converter = null;
            }
        }
    }

    public void stop() {
        // при закрытии приложения
        playing = false;
        if (playThread != null) {
            playThread.interrupt();
            try { playThread.join(1000); } catch (InterruptedException ignored) {}
        }
        cleanupGrabber();
    }
}
