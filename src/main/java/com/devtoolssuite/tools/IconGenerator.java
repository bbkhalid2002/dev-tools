package com.devtoolssuite.tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Utility to generate Windows ICO and macOS ICNS icon files from the source PNG.
 * - Reads icons/dev_tools_suite.png from the project root
 * - Writes icons/dev_tools_suite.ico and icons/dev_tools_suite.icns
 *
 * ICO: single 256x256 PNG-compressed entry (supported on modern Windows)
 * ICNS: single ic08 chunk (256x256 PNG)
 */
public class IconGenerator {
    public static void main(String[] args) throws Exception {
        File projectRoot = new File(System.getProperty("user.dir"));
        File iconsDir = new File(projectRoot, "icons");
        File pngFile = new File(iconsDir, "dev_tools_suite.png");

        if (!pngFile.isFile()) {
            System.err.println("Source PNG not found: " + pngFile.getAbsolutePath());
            System.exit(1);
        }

        BufferedImage src = ImageIO.read(pngFile);
        if (src == null) {
            throw new IOException("Failed to read PNG: " + pngFile.getAbsolutePath());
        }

        // Ensure a 256x256 version exists for ICO/ICNS
        BufferedImage img256 = scale(src, 256, 256);
        byte[] png256 = toPNG(img256);

        File icoOut = new File(iconsDir, "dev_tools_suite.ico");
        File icnsOut = new File(iconsDir, "dev_tools_suite.icns");

        writeIco(icoOut, png256);
        writeIcns(icnsOut, png256);

        System.out.println("ICO written:  " + icoOut.getAbsolutePath());
        System.out.println("ICNS written: " + icnsOut.getAbsolutePath());
    }

    private static BufferedImage scale(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }

    private static byte[] toPNG(BufferedImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!ImageIO.write(img, "png", baos)) {
            throw new IOException("No PNG writer available");
        }
        return baos.toByteArray();
    }

    private static void writeIco(File out, byte[] pngData) throws IOException {
        // ICO uses little-endian fields
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // ICONDIR header (6 bytes): reserved(2)=0, type(2)=1, count(2)=1
        writeLEShort(baos, 0);
        writeLEShort(baos, 1);
        writeLEShort(baos, 1);

        // ICONDIRENTRY (16 bytes)
        int width = 256;
        int height = 256;
        baos.write(width == 256 ? 0 : width);   // bWidth: 0 means 256
        baos.write(height == 256 ? 0 : height); // bHeight: 0 means 256
        baos.write(0); // bColorCount
        baos.write(0); // bReserved
        writeLEShort(baos, 1);  // wPlanes (ignored for PNG, commonly 1)
        writeLEShort(baos, 32); // wBitCount (ignored for PNG, commonly 32)
        writeLEInt(baos, pngData.length);       // dwBytesInRes
        writeLEInt(baos, 6 + 16);               // dwImageOffset (start of image data)

        // Image data (PNG for 256x256)
        baos.write(pngData);

        try (FileOutputStream fos = new FileOutputStream(out)) {
            baos.writeTo(fos);
        }
    }

    private static void writeIcns(File out, byte[] png256) throws IOException {
        // ICNS uses big-endian fields
        // File structure:
        // 'icns' (4) | totalLen (4 BE) | 'ic08' (4) | chunkLen (4 BE) | pngData
        int chunkLen = 8 + png256.length; // includes type+length header
        int totalLen = 8 + chunkLen;      // file header + chunk

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(new byte[]{'i','c','n','s'});
        writeBEInt(baos, totalLen);
        baos.write(new byte[]{'i','c','0','8'});
        writeBEInt(baos, chunkLen);
        baos.write(png256);

        try (FileOutputStream fos = new FileOutputStream(out)) {
            baos.writeTo(fos);
        }
    }

    private static void writeLEShort(OutputStream os, int v) throws IOException {
        os.write(v & 0xFF);
        os.write((v >>> 8) & 0xFF);
    }

    private static void writeLEInt(OutputStream os, int v) throws IOException {
        os.write(v & 0xFF);
        os.write((v >>> 8) & 0xFF);
        os.write((v >>> 16) & 0xFF);
        os.write((v >>> 24) & 0xFF);
    }

    private static void writeBEInt(OutputStream os, int v) throws IOException {
        os.write((v >>> 24) & 0xFF);
        os.write((v >>> 16) & 0xFF);
        os.write((v >>> 8) & 0xFF);
        os.write(v & 0xFF);
    }
}
