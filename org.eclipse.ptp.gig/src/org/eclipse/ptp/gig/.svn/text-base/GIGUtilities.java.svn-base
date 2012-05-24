package org.eclipse.ptp.gig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class GIGUtilities {

	public static void processSource(String filename) throws IOException, InterruptedException, CoreException {
		if (filename.startsWith("L/")) { //$NON-NLS-1$
			filename = filename.substring(2);
			String folder = filename.substring(0, filename.indexOf('/'));
			String gigFolder = folder + "/" + "gig"; //$NON-NLS-1$ //$NON-NLS-2$
			String file = filename.substring(filename.lastIndexOf('/') + 1, filename.lastIndexOf('.'));
			String execFile = gigFolder + '/' + file + ".gig"; //$NON-NLS-1$

			// make the directory if necessary
			runCommandOnShell("ls");//"mkdir " + gigFolder); //$NON-NLS-1$

			// create the executable
			runCommandOnShell("klee-l++ " + filename + " -o " + execFile); //$NON-NLS-1$ //$NON-NLS-2$

			processExec(execFile);
		}
	}

	private static void processExec(String execFile) throws IOException, InterruptedException, CoreException {
		String logFile = execFile + ".log"; //$NON-NLS-1$
		runCommandOnShell("gklee " + execFile + " > " + logFile); //$NON-NLS-1$ //$NON-NLS-2$
		processLog(logFile);
	}

	private static void processLog(String logFile) throws CoreException, IOException {
		// TODO Must now access this logFile using IResources
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IPath rootPath = root.getFullPath();
		IPath logPath = rootPath.append('/' + logFile);
		IFile file = root.getFile(logPath);
		InputStream is = file.getContents();
		Scanner scan = new Scanner(is);

		processScanner(scan);

		is.close();
		scan.close();
	}

	private static void processScanner(Scanner scan) {
		// TODO Auto-generated method stub

	}

	private static void runCommandOnShell(String string) throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(string);
		process.waitFor();
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		Scanner scan = new Scanner(br);
		while (scan.hasNextLine()) {
			System.out.println(scan.nextLine());
		}
	}

}
