package androidx.pluginmgr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * 文件操作工具类
 * <p>
 * 使用nio以提高性能
 * 
 * @author HouKangxi
 *
 */
class FileUtil {

	public static void writeToFile(byte[] data, File target) throws IOException {
		FileOutputStream fo = null;
		ReadableByteChannel src = null;
		FileChannel out = null;
		try {
			src = Channels.newChannel(new ByteArrayInputStream(data));
			fo = new FileOutputStream(target);
			out = fo.getChannel();
			out.transferFrom(src, 0, data.length);
		} finally {
			if (fo != null) {
				fo.close();
			}
			if (src != null) {
				src.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * 
	 * 复制文件
	 * 
	 * @param source
	 *            - 源文件
	 * 
	 * @param target
	 *            - 目标文件
	 * 
	 */
	public static void copyFile(File source, File target) {

		FileInputStream fi = null;
		FileOutputStream fo = null;

		FileChannel in = null;

		FileChannel out = null;

		try {
			fi = new FileInputStream(source);

			fo = new FileOutputStream(target);

			in = fi.getChannel();// 得到对应的文件通道

			out = fo.getChannel();// 得到对应的文件通道

			in.transferTo(0, in.size(), out);// 连接两个通道，并且从in通道读取，然后写入out通道

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fi.close();

				in.close();

				fo.close();

				out.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
