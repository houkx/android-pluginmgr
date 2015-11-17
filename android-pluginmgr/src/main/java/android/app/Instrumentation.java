package android.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * @author Lody
 * @version 1.0
 */
public class Instrumentation {


    public void onCreate(Bundle arguments) {

    }


    public void start() {

    }


    public void onStart() {

    }


    public boolean onException(Object obj, Throwable e) {

        return false;
    }


    public void sendStatus(int resultCode, Bundle results) {

    }


    public void finish(int resultCode, Bundle results) {

    }


    public void setAutomaticPerformanceSnapshots() {

    }


    public void startPerformanceSnapshot() {

    }


    public void endPerformanceSnapshot() {

    }


    public void onDestroy() {

    }


    public Context getContext() {
        return null;
    }


    public ComponentName getComponentName() {
        return null;
    }


    public Context getTargetContext() {
        return null;
    }


    public boolean isProfiling() {
        return false;
    }


    public void startProfiling() {

    }


    public void stopProfiling() {

    }


    public void setInTouchMode(boolean inTouch) {

    }


    public void waitForIdle(Runnable recipient) {

    }


    public void waitForIdleSync() {

    }


    public void runOnMainSync(Runnable runner) {

    }


    public Activity startActivitySync(Intent intent) {

        return null;
    }


    public void addMonitor(ActivityMonitor monitor) {

    }


    public Instrumentation.ActivityMonitor addMonitor(IntentFilter filter, ActivityResult result, boolean block) {
        return null;
    }


    public ActivityMonitor addMonitor(String cls, ActivityResult result, boolean block) {
        return null;
    }


    public boolean checkMonitorHit(ActivityMonitor monitor, int minHits) {
        return false;
    }


    public Activity waitForMonitor(ActivityMonitor monitor) {
        return null;
    }


    public Activity waitForMonitorWithTimeout(ActivityMonitor monitor, long timeOut) {
        return null;
    }


    public void removeMonitor(ActivityMonitor monitor) {

    }


    public boolean invokeMenuActionSync(Activity targetActivity, int requestCode, int flag) {
        return false;
    }


    public boolean invokeContextMenuAction(Activity targetActivity, int requestCode, int flag) {
        return false;
    }


    public void sendStringSync(String text) {

    }


    public void sendKeySync(KeyEvent event) {

    }


    public void sendKeyDownUpSync(int key) {

    }


    public void sendCharacterSync(int keyCode) {

    }


    public void sendPointerSync(MotionEvent event) {

    }


    public void sendTrackballEventSync(MotionEvent event) {

    }


    public Application newApplication(ClassLoader cl, String className, Context who) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return null;
    }


    public void callApplicationOnCreate(Application app) {

    }


    public Activity newActivity(Class<?> clazz, Context who, IBinder token, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance) throws InstantiationException, IllegalAccessException {
        return null;
    }


    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return null;
    }


    public void callActivityOnCreate(Activity target, Bundle icicle) {

    }


    public void callActivityOnDestroy(Activity target) {

    }


    public void callActivityOnRestoreInstanceState(Activity target, Bundle savedInstanceState) {

    }


    public void callActivityOnPostCreate(Activity target, Bundle icicle) {

    }


    public void callActivityOnNewIntent(Activity target, Intent intent) {

    }


    public void callActivityOnStart(Activity target) {

    }


    public void callActivityOnRestart(Activity target) {

    }


    public void callActivityOnResume(Activity target) {

    }


    public void callActivityOnStop(Activity target) {
    }


    public void callActivityOnSaveInstanceState(Activity target, Bundle outState) {
    }


    public void callActivityOnPause(Activity target) {

    }


    public void callActivityOnUserLeaving(Activity target) {

    }


    public void startAllocCounting() {
    }


    public void stopAllocCounting() {
    }


    public Bundle getAllocCounts() {
        return null;
    }


    public Bundle getBinderCounts() {
        return null;
    }


    public UiAutomation getUiAutomation() {
        return null;
    }


    public ActivityResult execStartActivity(final Context who, final IBinder contextThread, final IBinder token, final Activity target, final Intent intent, final int requestCode) {
        return null;
    }

    public ActivityResult execStartActivity(final Context who, final IBinder contextThread, final IBinder token, final Activity target, final Intent intent, final int requestCode, final Bundle options) {
        return null;
    }

    public ActivityResult execStartActivity(final Context who, final IBinder contextThread, final IBinder token, final Fragment fragment, final Intent intent, final int requestCode) {
        return null;
    }

    public ActivityResult execStartActivity(final Context who, final IBinder contextThread, final IBinder token, final Fragment fragment, final Intent intent, final int requestCode, final Bundle options) {
        return null;
    }


    protected static final class ActivityMonitor {
    }

    public static final class ActivityResult {
    }

}
