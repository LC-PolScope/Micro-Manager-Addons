/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mbl.cdp.mmx.topframe;

import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author GBH
 */
public class TestClassLoaderPath {
	public static void main(String[] args) {
		new TestClassLoaderPath().listClassLoaderPaths();
	}
	public void listClassLoaderPaths() {
		
		URL[] urls = ((URLClassLoader) (Thread.currentThread().getContextClassLoader())).getURLs();
		System.out.println("ClassLoader classpath URLs for thread (" +
				Thread.currentThread().getName() + "):\n");
		for (URL url : urls) {
				System.out.println(url);
		}
	}
	
}
