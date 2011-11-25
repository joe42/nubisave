package com.github.joe42.splitter;

import com.github.joe42.splitter.backend.Mounter;

import fuse.FuseMount;

public class Main {
	public static void main(String[] args) {
		if (args.length < 4) {
			System.out
					.println("Must specify mountpoint folder_with_storage_mountpoints");
			System.exit(-1);
		}

		String fuseArgs[] = new String[args.length-1];
		System.arraycopy(args, 0, fuseArgs, 0, fuseArgs.length);
		// System.out.println(fuseArgs[0]);
		try {
			Mounter mounter = new Mounter(args[3]);
			Splitter splitter = new Splitter(mounter.getServices()); 
			FuseMount.mount(fuseArgs, new ConfigurableFuseBox(splitter, mounter));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
