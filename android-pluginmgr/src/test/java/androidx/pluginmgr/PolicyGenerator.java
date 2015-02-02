/*
 * Copyright (C) 2015 HouKx <hkx.aidream@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.pluginmgr;
import static java.lang.reflect.Modifier.FINAL;
import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PUBLIC;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import android.content.Context;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import com.google.dexmaker.Code;
import com.google.dexmaker.DexMaker;
import com.google.dexmaker.FieldId;
import com.google.dexmaker.Local;
import com.google.dexmaker.MethodId;
import com.google.dexmaker.TypeId;
import com.google.dexmaker.dx.dex.DexFormat;

/**
 * 生成以下代码：
 * <pre>
 * public class androidx.pluginmgr.Policy implements 
 *                com.android.internal.policy.IPolicy {
 *                
 *     private com.android.internal.policy.IPolicy mBase;
 *     
 *     public Policy(IPolicy base){
 *         this.mBase = base;
 *     }
 *     
 *     public android.view.Window makeNewWindow(Context context) {
 *     	   return new androidx.pluginmgr.PLWindow(context);
 *     }
 * 
 *     public android.view.LayoutInflater makeNewLayoutInflater(Context context) {
 *         return mBase.makeNewLayoutInflater(context);
 *     }
 * 
 *     public android.view.WindowManagerPolicy makeNewWindowManager() {
 *         return mBase.makeNewWindowManager();
 *     }
 * 
 *     public android.view.FallbackEventHandler makeNewFallbackEventHandler(Context context) {
 *         return mBase.makeNewFallbackEventHandler(context);
 *     }
 * }
 * </pre>
 * 
 * @author HouKangxi
 *
 */
class PolicyGenerator {
	public void createDex(File saveTo) throws IOException {
		byte[] dex = createDex();
		if (saveTo.getName().endsWith(".dex")) {
			FileUtil.writeToFile(dex, saveTo);
		} else {
			JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(
					saveTo));
			jarOut.putNextEntry(new JarEntry(DexFormat.DEX_IN_JAR_NAME));
			jarOut.write(dex);
			jarOut.closeEntry();
			jarOut.close();
		}
	}

	@SuppressWarnings("unchecked")
	private <S,D extends S>byte[] createDex() {
		final String superClassName = "Ljava/lang/Object;";
		final String targetClassName = "Landroidx/pluginmgr/Policy;";
		final String iPolicyClassName ="Lcom/android/internal/policy/IPolicy;"; 
		TypeId<D> type = TypeId.get(targetClassName);
		TypeId<S> superType = TypeId.get(superClassName);
		TypeId<?> IPolicy = TypeId.get(iPolicyClassName);
		DexMaker dexMaker = new DexMaker();
		final String windowClassName = "Landroidx/pluginmgr/PhoneWindow;";
		final String superWindowClassName = "Lcom/android/internal/policy/impl/PhoneWindow;";
		createWindowDex(dexMaker,windowClassName,superWindowClassName);
		dexMaker.declare(type, "", PUBLIC|FINAL, superType, IPolicy);
		FieldId<D, ?> fieldBase = type.getField(IPolicy, "mBase");
		{
			//  private com.android.internal.policy.IPolicy mBase;
			dexMaker.declare(fieldBase, PRIVATE, null);
		}
		// constructor(IPolicy base)
		{
			MethodId<D, Void> method = type.getConstructor(IPolicy);
			Code code = dexMaker.declare(method, PUBLIC);
			Local<D> localThis = code.getThis(type);
			@SuppressWarnings("rawtypes")
			Local pBasePolicy = code.getParameter(0, IPolicy);
			
			MethodId<S, Void> superConstructor = superType.getConstructor();
			code.invokeDirect(superConstructor, null, localThis);
			code.iput(fieldBase, localThis, pBasePolicy);
			code.returnVoid();
		}
		TypeId<Context> Context = TypeId.get(Context.class);
		{
			// public android.view.Window makeNewWindow(Context context) {
			//	return new androidx.pluginmgr.PLWindow(context);
		    //}
			TypeId<Window> Window = TypeId.get(Window.class);
			MethodId<D,Window> makeNewWindow = type.getMethod(Window, "makeNewWindow", Context);
			Code code = dexMaker.declare(makeNewWindow, PUBLIC);
			Local<Context> pContext = code.getParameter(0, Context);
			Local<Window> localWindow = code.newLocal(Window);
			TypeId<Window> MyWindow = TypeId.get(windowClassName);
			@SuppressWarnings("rawtypes")
			MethodId newWindow = MyWindow.getConstructor(Context);
			code.newInstance(localWindow, newWindow, pContext);
			code.returnValue(localWindow);
			
		}
		{
//			      public android.view.LayoutInflater makeNewLayoutInflater(Context context) {
//				         return mBase.makeNewLayoutInflater(context);
//				  }
			 String methodName = "makeNewLayoutInflater";
             TypeId<LayoutInflater> LayoutInflater = TypeId.get(LayoutInflater.class);
             MethodId<D, LayoutInflater> makeNewLayoutInflater = type.getMethod(LayoutInflater, methodName, Context);
             MethodId<?, LayoutInflater> policy_makeNewLayoutInflater = IPolicy.getMethod(LayoutInflater, methodName, Context);
             Code code = dexMaker.declare(makeNewLayoutInflater, PUBLIC);
             Local<D> localThis = code.getThis(type);
             Local<Context> pContext = code.getParameter(0, Context);
             @SuppressWarnings("rawtypes")
			 Local localPolicy = code.newLocal(IPolicy);
             Local<LayoutInflater> localLyft=code.newLocal(LayoutInflater);
             code.iget(fieldBase, localPolicy, localThis);
             code.invokeInterface(policy_makeNewLayoutInflater, localLyft, localPolicy, pContext);
             code.returnValue(localLyft);
		}
		{
//		      public android.view.WindowManagerPolicy makeNewWindowManager() {
//	          		return mBase.makeNewWindowManager();
//	          }
			String methodName = "makeNewWindowManager";
			TypeId<?> WinMgrPolicy = TypeId.get("Landroid/view/WindowManagerPolicy;");
			MethodId<D, ?> makeNewWinmgrPolicy = type.getMethod(WinMgrPolicy, methodName);
			MethodId<?, ?> policy_makeNewLayoutInflater = IPolicy.getMethod(WinMgrPolicy, methodName);
			Code code = dexMaker.declare(makeNewWinmgrPolicy, PUBLIC);
			Local<D> localThis = code.getThis(type);
			@SuppressWarnings("rawtypes")Local localPolicy = code.newLocal(IPolicy);
			@SuppressWarnings("rawtypes")Local localWmgrPolicy = code.newLocal(WinMgrPolicy);
			code.iget(fieldBase, localPolicy, localThis);
			code.invokeInterface(policy_makeNewLayoutInflater, localWmgrPolicy, localPolicy);
			code.returnValue(localWmgrPolicy);
		}
		{
//			  
//		      public android.view.FallbackEventHandler makeNewFallbackEventHandler(Context context) {
//		          return mBase.makeNewFallbackEventHandler(context);
//		      }
			String methodName = "makeNewFallbackEventHandler";
            TypeId<?> EvtHandler = TypeId.get("Landroid/view/FallbackEventHandler;");
            MethodId<D, ?> makeNewFallbackEventHandler = type.getMethod(EvtHandler, methodName, Context);
            MethodId<?, ?> policy_makeNewLayoutInflater = IPolicy.getMethod(EvtHandler, methodName, Context);
			Code code = dexMaker.declare(makeNewFallbackEventHandler, PUBLIC);
			Local<D> localThis = code.getThis(type);
			Local<Context> pContext = code.getParameter(0, Context);
			@SuppressWarnings("rawtypes")Local localPolicy = code.newLocal(IPolicy);
			@SuppressWarnings("rawtypes")Local localEvtH = code.newLocal(EvtHandler);
			code.iget(fieldBase, localPolicy, localThis);
			code.invokeInterface(policy_makeNewLayoutInflater, localEvtH,localPolicy, pContext);
			code.returnValue(localEvtH);
		}
		return dexMaker.generate();
	}
	/**
	 * 生成以下代码：
	 * <pre>
	 *  public class androidx.pluginmgr.PhoneWindow extends 
	 *     com.android.internal.policy.impl.PhoneWindow{
	 *     
	 *     public PLWindow(Context context){
	 *       super(context);
	 *     }
	 *     
	 *     public void setWindowManager(WindowManager wm,
	 *         IBinder appToken, String appName,boolean hardwareAccelerated) {
	 *        ActivityOverrider.changeActivityInfo(getContext());
	 *        super.setWindowManager(wm,appToken,appName,hardwareAccelerated);
	 *     }
	 *  }
	 * </pre>
	 * @return
	 */
	private <S,D extends S>void createWindowDex(DexMaker dexMaker,String windowClassName,String superClassName) {
		TypeId<D> type = TypeId.get(windowClassName);
		TypeId<S> superType = TypeId.get(superClassName);
		dexMaker.declare(type, "", PUBLIC|FINAL, superType);
		// constructor(Context)
		TypeId<Context> Context = TypeId.get(Context.class);
		{
			MethodId<D, Void> method = type.getConstructor(Context);
			Code code = dexMaker.declare(method, PUBLIC);
			Local<Context> pCtx = code.getParameter(0, Context);
			Local<D> localThis = code.getThis(type);
			MethodId<S, Void> superConstructor = superType.getConstructor(Context);
			code.invokeDirect(superConstructor, null, localThis, pCtx);
			code.returnVoid();
		}
		// public void setWindowManager(WindowManager wm, IBinder appToken, String appName,boolean hardwareAccelerated) 
		{
			final String methodName = "setWindowManager";
			TypeId<WindowManager> WinMgr = TypeId.get(WindowManager.class);
			TypeId<IBinder> IBinder = TypeId.get(IBinder.class);
			TypeId<?>[] parameterTypes = new TypeId<?>[]{WinMgr,IBinder,TypeId.STRING,TypeId.BOOLEAN} ;
			MethodId<D, Void> method = type.getMethod(TypeId.VOID, methodName, parameterTypes);
			Code code = dexMaker.declare(method, PUBLIC);
			Local<?>[]parameters=new Local<?>[]{ code.getParameter(0, WinMgr)
					,code.getParameter(1, IBinder),code.getParameter(2, TypeId.STRING)
					,code.getParameter(3, TypeId.BOOLEAN)};
			Local<Context> localContext = code.newLocal(Context);
			Local<D> localThis = code.getThis(type);
			
			MethodId<S,Void> superMethod = superType.getMethod(TypeId.VOID, methodName, parameterTypes);
			TypeId<ActivityOverider> Overrider = TypeId.get(ActivityOverider.class);
			MethodId<ActivityOverider, Void> changeActivityInfo = Overrider.getMethod(TypeId.VOID, "changeActivityInfo", Context);
			MethodId<S, Context> getContext = superType.getMethod(Context, "getContext");
			code.invokeSuper(getContext, localContext, localThis);
			code.invokeStatic(changeActivityInfo, null, localContext);
			code.invokeSuper(superMethod, null, localThis, parameters);
			code.returnVoid();
		}
	}
	
}
