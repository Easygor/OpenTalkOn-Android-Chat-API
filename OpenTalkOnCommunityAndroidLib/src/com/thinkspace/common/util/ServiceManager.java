package com.thinkspace.common.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.IBinder;
import android.util.Log;

public class ServiceManager {
	private static final String SERVICE_MANAGER = "android.os.ServiceManager";
	private static final String GET_SERVICE_METHOD = "getService";
	private static final String AS_INTERFACE = "asInterface";
	public static final synchronized Object getServiceStub(String serviceName,
			String binderType) {

		try {

			return getServiceStubInternal(serviceName, binderType);
		} catch (Exception e) {

			Log.e("ServiceLocator", "", e);
			return null;
		}
	}

	private static final ClassLoader getClassLoader() {

		return ServiceManager.class.getClassLoader();
	}

	private static final Method getDeclaredMethod(Class<?> owner,
			String methodName, Class<?>... parameterTypes)
			throws SecurityException, NoSuchMethodException {

		Method m = owner.getDeclaredMethod(methodName, parameterTypes);

		if (!m.isAccessible()) {

			m.setAccessible(true);
		}

		return m;
	}

	private static final Method getGetServiceMethod(Class<?> serviceManager)
			throws SecurityException, NoSuchMethodException {

		return getDeclaredMethod(serviceManager, GET_SERVICE_METHOD,
				String.class);
	}

	private static final Object getServiceStub(Object binder, Class<?> stub)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, SecurityException,
			NoSuchMethodException, ClassNotFoundException {

		Method method = getAsInterfaceMethod(stub);

		return method.invoke(null, binder);
	}

	private static final Class<?> getServiceStubClass(String binderType)
			throws ClassNotFoundException {

		return getClassLoader().loadClass(binderType);
	}

	private static final Method getAsInterfaceMethod(
			Class<?> stub) throws SecurityException, NoSuchMethodException {

		return getDeclaredMethod(stub, AS_INTERFACE, IBinder.class);
	}

	private static final Class<?> getServiceManager()
			throws ClassNotFoundException {

		return getClassLoader().loadClass(SERVICE_MANAGER);
	}

	private static final Object getServiceStubInternal(String serviceName,
			String binderType) throws Exception {

		Class<?> serviceManager = getServiceManager();
		Method method = getGetServiceMethod(serviceManager);
		Object binder = method.invoke(null, serviceName);
		Class<?> stub = getServiceStubClass(binderType);
		Object service = getServiceStub(binder, stub);

		return service;
	}
}
