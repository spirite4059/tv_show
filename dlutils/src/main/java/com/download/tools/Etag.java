package com.download.tools;

import android.annotation.TargetApi;
import android.os.Build;

import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import com.google.common.io.ByteSource;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents the
 * <a href="http://docs.aws.amazon.com/AmazonS3/latest/API/mpUploadComplete.html" >eTag</a>
 * calculated by Amazon S3.
 */
public final class Etag {

    private final String md5;
    private final Integer partNumber;

    private static final Pattern MD5_PATTERN = Pattern.compile("[a-f0-9]{32}");
    private static final Pattern FULL_ETAG_PATTERN
            = Pattern.compile("(" + MD5_PATTERN.pattern() + ")(?:-([0-9]+))?");

    private Etag(final byte[] md5, final Integer partNumber) {
        this(md5asString(md5), partNumber);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String md5asString(final byte[] md5) {
        checkArgument(md5.length == 16);
        return bytesToHexString(md5);
    }

    private Etag(final String md5, final Integer partNumber) {
        checkArgument(MD5_PATTERN.matcher(md5).matches());
        checkArgument(partNumber == null || partNumber > 0);
        this.md5 = md5;
        this.partNumber = partNumber;
    }

    public String asString() {
        return md5 + (partNumber == null ? "" : "-" + partNumber);
    }

    public static Etag parse(final String string) {
        final Matcher matcher = FULL_ETAG_PATTERN.matcher(string);
        checkArgument(matcher.matches(), "Invalid format: " + string);
        final String md5 = matcher.group(1);
        final String partNumber = matcher.group(2);
        return new Etag(md5, partNumber == null ? null : Integer.parseInt(partNumber));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + asString() + "}";
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final Etag etag = (Etag) other;

        if (!md5.equals(etag.md5)) {
            return false;
        }
        if (partNumber != null ? !partNumber.equals(etag.partNumber) : etag.partNumber != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = md5.hashCode();
        result = 31 * result + (partNumber != null ? partNumber.hashCode() : 0);
        return result;
    }


    public static final long DEFAULT_MINIMUM_UPLOAD_PART_SIZE
            = new TransferManagerConfiguration().getMinimumUploadPartSize();


// =======
// Compute
// =======

    /**
     * Calculates {@link Etag} (MD5 checksum in the AWS way).
     * For small files (less than {@link #DEFAULT_MINIMUM_UPLOAD_PART_SIZE}, practically 5 GB )
     * it's the MD5. For big files, it's a MD5 of the MD5 of its multipart chunks.
     * <p>
     * http://permalink.gmane.org/gmane.comp.file-systems.s3.s3tools/583
     * https://github.com/Teachnova/s3md5
     * http://stackoverflow.com/questions/12186993/what-is-the-algorithm-to-compute-the-amazon-s3-etag-for-a-file-larger-than-5gb
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Etag compute(final ByteSource byteSource, final int chunkSize)
            throws IOException, DigestException {
        final List<byte[]> md5s = new ArrayList<>();
        try (final InputStream inputStream = byteSource.openBufferedStream()) {
            while (true) {
                if (inputStream.available() > 0) {
                    final byte[] md5 = computeMd5(inputStream, chunkSize);
                    md5s.add(md5);
                } else {
                    break;
                }
            }
        }
        if (md5s.size() == 1) {
            return new Etag(md5s.get(0), null);
        } else {
            final byte[] md5concatenation = new byte[md5s.size() * 16];
            for (int i = 0; i < md5s.size(); i++) {
                final byte[] md5 = md5s.get(i);
                System.arraycopy(md5, 0, md5concatenation, i * 16, 16);
            }
            final byte[] finalMd5 = DigestUtils.md5(md5concatenation);
            return new Etag(finalMd5, md5s.size());
        }
    }


    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Etag computeVideo(final ByteSource byteSource)
            throws IOException, DigestException {
        final List<byte[]> md5s = new ArrayList<>();
        try (final InputStream inputStream = byteSource.openBufferedStream()) {
            while (true) {
                if (inputStream.available() > 0) {
                    final byte[] md5 = computeMd5(inputStream, 67108864);
                    md5s.add(md5);
                } else {
                    break;
                }
            }
        }
        if (md5s.size() == 1) {
            return new Etag(md5s.get(0), null);
        } else {
            final byte[] md5concatenation = new byte[md5s.size() * 16];
            for (int i = 0; i < md5s.size(); i++) {
                final byte[] md5 = md5s.get(i);
                System.arraycopy(md5, 0, md5concatenation, i * 16, 16);
            }
            final byte[] finalMd5 = DigestUtils.md5(md5concatenation);
            return new Etag(finalMd5, md5s.size());
        }
    }

    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Etag computeApk(final ByteSource byteSource)
            throws IOException, DigestException {
        final List<byte[]> md5s = new ArrayList<>();
        try (final InputStream inputStream = byteSource.openBufferedStream()) {
            while (true) {
                if (inputStream.available() > 0) {
                    final byte[] md5 = computeMd5(inputStream, 5242880);
                    md5s.add(md5);
                } else {
                    break;
                }
            }
        }
        if (md5s.size() == 1) {
            return new Etag(md5s.get(0), null);
        } else {
            final byte[] md5concatenation = new byte[md5s.size() * 16];
            for (int i = 0; i < md5s.size(); i++) {
                final byte[] md5 = md5s.get(i);
                System.arraycopy(md5, 0, md5concatenation, i * 16, 16);
            }
            final byte[] finalMd5 = DigestUtils.md5(md5concatenation);
            return new Etag(finalMd5, md5s.size());
        }
    }

    /*package*/
    static byte[] computeMd5(
            final InputStream inputStream,
            final int length
    ) throws IOException, DigestException {
        MessageDigest md5Digest = null;
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final byte[] buffer = new byte[8192];
        long totalRead = 0;
        while (true) {
            final long greatestRemainder = length - totalRead;
            final int sizeToRead = greatestRemainder > buffer.length
                    ? buffer.length : (int) greatestRemainder;
            final int read = inputStream.read(buffer, 0, sizeToRead);
            if (read > 0) {
                md5Digest.update(buffer, 0, read);
                totalRead += read;
            } else {
                return md5Digest.digest();
            }
        }
    }


//    public static void main(String[] args) throws DigestException, IOException {
//        final File file = new File("e:/logo1.jpg");
//        final int chunkSize = 64 * 1024 * 1024;
//        final long start = System.currentTimeMillis();
//        final Etag etag = Etag.compute(Files.asByteSource(file), chunkSize);
//        System.out.println(etag.toString());
//    }
}