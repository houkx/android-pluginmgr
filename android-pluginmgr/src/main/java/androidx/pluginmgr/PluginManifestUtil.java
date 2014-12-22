/**
 * 
 */
package androidx.pluginmgr;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * @author HouKangxi
 * 
 */
class PluginManifestUtil {
	static String setManifestInfo(Context context, String apkPath, PlugInfo info)
			throws XmlPullParserException, IOException {
		String manifestXML = XmlManifestReader.getManifestXMLFromAPK(apkPath);
		PackageInfo pkgInfo = context.getPackageManager()
				.getPackageArchiveInfo(
						apkPath,
						PackageManager.GET_ACTIVITIES
								| PackageManager.GET_RECEIVERS//
								| PackageManager.GET_PROVIDERS//
								| PackageManager.GET_META_DATA//
								| PackageManager.GET_SHARED_LIBRARY_FILES//
				// | PackageManager.GET_SERVICES//
				// | PackageManager.GET_SIGNATURES//
				);
		// Log.d("ManifestReader: setManifestInfo", "GET_SHARED_LIBRARY_FILES="
		// + pkgInfo.applicationInfo.nativeLibraryDir);
		info.setPackageInfo(pkgInfo);
		File libdir = ActivityOverider.getPluginLibDir(info.getId());
		if(extractLibFile(apkPath, libdir)){
			pkgInfo.applicationInfo.nativeLibraryDir=libdir.getAbsolutePath();
		}
		
		setAttrs(info, manifestXML);
		return info.getPackageInfo().packageName;
	}
	private static boolean extractLibFile(String apkPath, File tardir)
			throws  IOException {
		
		ZipFile zip = new ZipFile(new File(apkPath));
		
		String defaultArch = "armeabi";
        Map<String,List<ZipEntry>> archLibEntries = new HashMap<String, List<ZipEntry>>();
		for (Enumeration<? extends ZipEntry> e = zip.entries(); e
				.hasMoreElements();) {
			ZipEntry entry = e.nextElement();
			String name = entry.getName();
			if (name.startsWith("/")) {
				name = name.substring(1);
			}
			if (name.startsWith("lib/")) {
				if(entry.isDirectory()){
					continue;
				}
				int sp = name.indexOf('/', 4);
				String osArch;
				if (sp > 0) {
					 osArch = name.substring(4, sp).toLowerCase();
				} else {
					osArch=defaultArch;
				}
				List<ZipEntry> alist = archLibEntries.get(osArch);
				if (alist == null) {
					alist = new LinkedList<ZipEntry>();
					archLibEntries.put(osArch, alist);
				}
				alist.add(entry);
			}
		}
		boolean rs = false;
		String arch = System.getProperty("os.arch");
		List<ZipEntry> alist = archLibEntries.get(arch.toLowerCase());
		if (alist == null) {
			alist = archLibEntries.get(defaultArch);
		}
		if (alist != null) {
			rs = true;
			if (!tardir.exists()) {
				tardir.mkdirs();
			}
			for (ZipEntry libEntry : alist) {
				String ename = libEntry.getName();
				String pureName = ename.substring(ename.lastIndexOf('/') + 1);
				File target = new File(tardir, pureName);
				FileUtil.writeToFile(zip.getInputStream(libEntry), target);
			}
		}
		zip.close();
		return rs;
	}
	private static void setAttrs(PlugInfo info, String manifestXML)
			throws XmlPullParserException, IOException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser parser = factory.newPullParser();
		parser.setInput(new StringReader(manifestXML));
		int eventType = parser.getEventType();
		String namespaceAndroid = null;
		do {
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT: {
				break;
			}
			case XmlPullParser.START_TAG: {
				String tag = parser.getName();
				if (tag.equals("manifest")) {
					namespaceAndroid = parser.getNamespace("android");
				} else if ("activity".equals(parser.getName())) {
					addActivity(info, namespaceAndroid, parser);
				} else if ("receiver".equals(parser.getName())) {
					addReceiver(info, namespaceAndroid, parser);
				} else if ("service".equals(parser.getName())) {
					addService(info, namespaceAndroid, parser);
				}else if("application".equals(parser.getName())){
					parseApplicationInfo(info, namespaceAndroid, parser);
				}
				break;
			}
			case XmlPullParser.END_TAG: {
				break;
			}
			}
			eventType = parser.next();
		} while (eventType != XmlPullParser.END_DOCUMENT);
	}

	private static void parseApplicationInfo(PlugInfo info,
			String namespace, XmlPullParser parser) throws XmlPullParserException, IOException{
		String applicationName = parser.getAttributeValue(namespace, "name");
		String packageName = info.getPackageInfo().packageName;
		ApplicationInfo applicationInfo = info.getPackageInfo().applicationInfo;
		applicationInfo.name = getName(applicationName, packageName);
	}

	private static void addActivity(PlugInfo info, String namespace,
			XmlPullParser parser) throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
		String activityName = parser.getAttributeValue(namespace, "name");
		String packageName = info.getPackageInfo().packageName;
		activityName = getName(activityName, packageName);
		ResolveInfo act = new ResolveInfo();
		act.activityInfo = info.findActivityByClassName(activityName);
		do {
			switch (eventType) {
			case XmlPullParser.START_TAG: {
				String tag = parser.getName();
				if ("intent-filter".equals(tag)) {
					if (act.filter == null) {
						act.filter = new IntentFilter();
					}
				} else if ("action".equals(tag)) {
					String actionName = parser.getAttributeValue(namespace,
							"name");
					act.filter.addAction(actionName);
				} else if ("category".equals(tag)) {
					String category = parser.getAttributeValue(namespace,
							"name");
					act.filter.addCategory(category);
				} else if ("data".equals(tag)) {
					// TODO parse data
				}
				break;
			}
			}
			eventType = parser.next();
		} while (!"activity".equals(parser.getName()));
		//
		info.addActivity(act);
	}

	private static void addService(PlugInfo info, String namespace,
			XmlPullParser parser) throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
		String serviceName = parser.getAttributeValue(namespace, "name");
		String packageName = info.getPackageInfo().packageName;
		serviceName = getName(serviceName, packageName);
		ResolveInfo service = new ResolveInfo();
		service.serviceInfo = info.findServiceByClassName(serviceName);
		do {
			switch (eventType) {
			case XmlPullParser.START_TAG: {
				String tag = parser.getName();
				if ("intent-filter".equals(tag)) {
					if (service.filter == null) {
						service.filter = new IntentFilter();
					}
				} else if ("action".equals(tag)) {
					String actionName = parser.getAttributeValue(namespace,
							"name");
					service.filter.addAction(actionName);
				} else if ("category".equals(tag)) {
					String category = parser.getAttributeValue(namespace,
							"name");
					service.filter.addCategory(category);
				} else if ("data".equals(tag)) {
					// TODO parse data
				}
				break;
			}
			}
			eventType = parser.next();
		} while (!"service".equals(parser.getName()));
		//
		info.addService(service);
	}

	private static void addReceiver(PlugInfo info, String namespace,
			XmlPullParser parser) throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
		String receiverName = parser.getAttributeValue(namespace, "name");
		String packageName = info.getPackageInfo().packageName;
		receiverName = getName(receiverName, packageName);
		ResolveInfo receiver = new ResolveInfo();
		// 此时的activityInfo 表示 receiverInfo
		receiver.activityInfo = info.findReceiverByClassName(receiverName);
		do {
			switch (eventType) {
			case XmlPullParser.START_TAG: {
				String tag = parser.getName();
				if ("intent-filter".equals(tag)) {
					if (receiver.filter == null) {
						receiver.filter = new IntentFilter();
					}
				} else if ("action".equals(tag)) {
					String actionName = parser.getAttributeValue(namespace,
							"name");
					receiver.filter.addAction(actionName);
				} else if ("category".equals(tag)) {
					String category = parser.getAttributeValue(namespace,
							"name");
					receiver.filter.addCategory(category);
				} else if ("data".equals(tag)) {
					// TODO parse data
				}
				break;
			}
			}
			eventType = parser.next();
		} while (!"receiver".equals(parser.getName()));
		//
		info.addReceiver(receiver);
	}

	private static String getName(String nameOrig, String pkgName) {
		if (nameOrig == null) {
			return null;
		}
		StringBuilder sb = null;
		if (nameOrig.startsWith(".")) {
			sb = new StringBuilder();
			sb.append(pkgName);
			sb.append(nameOrig);
		} else if (!nameOrig.contains(".")) {
			sb = new StringBuilder();
			sb.append(pkgName);
			sb.append('.');
			sb.append(nameOrig);
		} else {
			return nameOrig;
		}
		return sb.toString();
	}
}
