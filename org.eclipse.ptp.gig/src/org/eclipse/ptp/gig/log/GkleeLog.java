package org.eclipse.ptp.gig.log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GkleeLog {
	// the member variables parsed from the log file
	public final int concurrencyBugCheckingLevel, numBIs, numBCs, numInstructions, warpsWithBC, numberOfWarps,
			BIsWithBCInSharedMemory, sharedMemoryIndex, pathIndex, aveWarpBankConflictRate, aveBCWarp, aveWarp,
			aveBIBankConflictRate, aveBCBI, aveBI, numReadInstructionsWithMC, numReadInstructions, numWriteInstructionsWithMC,
			numWriteInstructions, numWarpsWithMC, numBIsWithMC, aveWarpMemoryCoalescingRate, aveMCWarp, aveBIMemoryCoalescingRate,
			aveMCBI, numWarpsWithWD, numBIsWithWD, aveWarpDivergentRate, aveWDWarp, aveBIWarpDivergenceRate, aveWDBI;
	public final List<ThreadBankConflict> threadBankConflicts = new ArrayList<ThreadBankConflict>();
	public final List<WarpDivergence> warpDivergences = new ArrayList<WarpDivergence>();
	public final List<ThreadBankConflict> threadBankConflictsAcrossWarps = new ArrayList<ThreadBankConflict>();
	public final List<MemoryCoalescingByWholeWarp> memoryCoalescingByWholeWarpList = new ArrayList<MemoryCoalescingByWholeWarp>();
	public int[] threadsThatWaitAtExplicitSyncThreads, threadsThatWaitAtReConvergentPoint;

	// the regular expressions for parsing the log file
	private final Pattern concurrencyBugcheckingLevelPattern = Pattern
			.compile("Configuration: concurrency bug checking level is: (\\d+)"); //$NON-NLS-1$
	private final Pattern deadlockCheckingAtBlockPattern = Pattern.compile("Deadlock Checking at Block (\\d+): "); //$NON-NLS-1$
	private final Pattern startCheckingBankConflictsAtSharedMemoryPattern = Pattern
			.compile("\\** Start checking bank conflicts at SharedMemory (\\d+)... \\**"); //$NON-NLS-1$
	private final Pattern readSetIsEmptyInBankConflictPattern = Pattern
			.compile("The read set is empty in bank conflict checking for capability 2.x"); //$NON-NLS-1$
	private final Pattern gkleeCapacityBankConflictPattern = Pattern
			.compile("GKLEE: \\** CAPACITY 2.x Bank Conflict \\**"); //$NON-NLS-1$
	private final Pattern threadsIncurABankConflictPattern = Pattern
			.compile("GKLEE: Threads (\\d+) and (\\d+) incur a (.+) bank conflict on bank (\\d+)"); //$NON-NLS-1$
	//private final Pattern gkleeInstPattern = Pattern.compile("\\[GKLEE\\] Inst: "); //$NON-NLS-1$
	private final Pattern instructionLineInFilePattern = Pattern
			.compile("Instruction Line: (\\d+), In File: (.+), With Dir Path: (.+)"); //$NON-NLS-1$
	private final Pattern fileLineInstructionPattern = Pattern
			.compile("\\[File: (.+), Line: (\\d+), Inst:\\p{Space}+(.+)\\]"); //$NON-NLS-1$
	private final Pattern kernelSharedBlockPattern = Pattern
			.compile("<(.+), (\\d+), b(\\d+), t(\\d+)> "); //$NON-NLS-1$
	private final Pattern kernelSharedBlockPattern2 = Pattern
			.compile("<(.+), (\\d+):(\\d+), b(\\d+), t(\\d+)> "); //$NON-NLS-1$
	private final Pattern capacityInstByWholeWarpPattern = Pattern
			.compile("GKLEE: \\** CAPACITY 2.x Inst By Whole Warp \\( (\\d+) \\) \\**"); //$NON-NLS-1$
	private final Pattern wordSizeAccessedByThreadsPattern = Pattern
			.compile("GKLEE: The word size accessed by threads: (\\d+), the (\\d+)th request over total (\\d+) requests"); //$NON-NLS-1$
	private final Pattern requestCoalescedIntoMemorySegmentPattern = Pattern
			.compile("GKLEE: This request is coalesced into one memory segment \\((\\d+) Bytes\\) accessed by (\\d+) threads"); //$NON-NLS-1$
	private final Pattern warpThreadsDivergedPattern = Pattern
			.compile("In warp (\\d+), threads are diverged into following sub-sets: "); //$NON-NLS-1$
	private final Pattern setPattern = Pattern.compile("Set (\\d+):"); //$NON-NLS-1$
	private final Pattern threadSetPattern = Pattern.compile("(Thread (\\d+) , )+"); //$NON-NLS-1$
	private final Pattern pathNumExploredPattern = Pattern.compile("path num explored here: (\\d+)"); //$NON-NLS-1$
	private final Pattern threadsWaitExplicitPattern = Pattern
			.compile("Threads \\((\\d+, )+\\) wait at the explicit __syncthreads\\(\\)"); //$NON-NLS-1$
	private final Pattern threadsWaitReConvPointPattern = Pattern
			.compile("Threads \\((\\d+, )+\\) wait at the re-convergence point, not __syncthreads\\(\\)"); //$NON-NLS-1$
	private final Pattern numberPattern = Pattern.compile("\\(?(\\d+),"); //$NON-NLS-1$
	private final Pattern acrossDifferentWarpsThreadsRacePattern = Pattern
			.compile("GKLEE: Across different warps, threads (\\d+) and (\\d+) incur a (.+) race \\(Actual\\) on "); //$NON-NLS-1$
	private final Pattern bankConflictInstructionPattern = Pattern
			.compile("Across (\\d+) BIs, the total num of instructions with BC : (\\d+), the total num of instructions: (\\d+)"); //$NON-NLS-1$
	private final Pattern bankConflictWarpPattern = Pattern
			.compile("Across (\\d+) BIs, the total num of warps with BC : (\\d+), the total num of warps: (\\d+)"); //$NON-NLS-1$
	private final Pattern bankConflictSharedPattern = Pattern
			.compile("In shared memory (\\d+), num of BIs with BC: (\\d+), num of BIs: (\\d+)"); //$NON-NLS-1$
	private final Pattern averageWarpBankConflictRatePattern = Pattern
			.compile("GKLEE: The Average 'Warp' Bank Conflict Rate for all shared memories at path (\\d+) : (\\d+)%, <avgBCWarp, avgWarp> : <(\\d+), (\\d+)>"); //$NON-NLS-1$
	private final Pattern averageBIBankConflictRatePattern = Pattern
			.compile("GKLEE: The Average 'BI' Bank Conflict Rate for all shared memories at path (\\d+) : (\\d+)%, <avgBCBI, avgBI> : <(\\d+), (\\d+)>"); //$NON-NLS-1$
	private final Pattern memoryCoalescingReadPattern = Pattern
			.compile("Across (\\d+) BIs, the total num of read instructions with MC: (\\d+), the total number of read instructions: (\\d+)"); //$NON-NLS-1$
	private final Pattern memoryCoalescingWritePattern = Pattern
			.compile("Across (\\d+) BIs, the total num of write instructions with MC: (\\d+), the total number of write instructions: (\\d+)"); //$NON-NLS-1$
	private final Pattern memoryCoalescingWarpsPattern = Pattern
			.compile("Across (\\d+) BIs, the total num of warps with MC: (\\d+), the total num of warps: (\\d+)"); //$NON-NLS-1$
	private final Pattern memoryCoalescingBIsPattern = Pattern.compile("num of BIs with MC: (\\d+), num of BIs: (\\d+)"); //$NON-NLS-1$
	private final Pattern averageWarpMemoryCoalescingRatePattern = Pattern
			.compile("GKLEE: The Average 'Warp' Memory Coalescing Rate at path (\\d+) : (\\d+)%, <avgMCWarp, avgWarp> : <(\\d+), (\\d+)>"); //$NON-NLS-1$
	private final Pattern averageBIMemoryCoalescingRatePattern = Pattern
			.compile("GKLEE: The Average 'BI' Memory Coalescing Rate at path (\\d+) : (\\d+)%, <avgMCBI, avgBI> : <(\\d+), (\\d+)>"); //$NON-NLS-1$
	private final Pattern warpDivergenceWarpPattern = Pattern
			.compile("Across (\\d+) BIs, the total num of warps with WD: (\\d+), the total num of warps: (\\d+)"); //$NON-NLS-1$
	private final Pattern warpDivergenceBIPattern = Pattern.compile("num of BIs with WD: (\\d+), num of BIs: (\\d+)"); //$NON-NLS-1$
	private final Pattern averageWarpWarpDivergenceRate = Pattern
			.compile("GKLEE: The Average 'Warp' Warp Divergence Rate at path (\\d+) : (\\d+)%, <avgWDWarp, avgWarp> : <(\\d+), (\\d+)>"); //$NON-NLS-1$
	private final Pattern averageBIWarpDivergenceRate = Pattern
			.compile("GKLEE: The Average 'BI' Warp Divergence Rate at path (\\d+) : (\\d+)%, <avgWDBI, avgBI> : <(\\d+), (\\d+)>"); //$NON-NLS-1$

	public GkleeLog(InputStream logInputStream) throws LogException {
		Scanner scanner = new Scanner(logInputStream);
		Scanner scan;

		String line = scanner.nextLine();
		Matcher matcher = this.concurrencyBugcheckingLevelPattern.matcher(line);
		// TODO the variable "matches" is mostly for debug purposes at this moment (except where used for program flow)
		boolean matches = matcher.matches();
		// TODO group needs to be parsed and put into variables as needed
		String group = matcher.group(1);
		this.concurrencyBugCheckingLevel = Integer.parseInt(group);

		scanner.nextLine();
		scanner.nextLine();
		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.deadlockCheckingAtBlockPattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		// TODO find how it groups blocks together

		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.startCheckingBankConflictsAtSharedMemoryPattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		// TODO find if we need this

		line = scanner.nextLine();
		matcher = this.readSetIsEmptyInBankConflictPattern.matcher(line);
		matches = matcher.matches();
		if (!matches) {
			// This indicates that there are in fact some bank conflicts
			line = scanner.nextLine();
			matcher = this.gkleeCapacityBankConflictPattern.matcher(line);
			matches = matcher.matches();
			while (matches) {
				scanner.nextLine();

				line = scanner.nextLine();
				matcher = this.threadsIncurABankConflictPattern.matcher(line);
				matches = matcher.matches();
				group = matcher.group(1);
				int tid1 = Integer.parseInt(group);
				group = matcher.group(2);
				int tid2 = Integer.parseInt(group);
				// either R-R or W-W (maybe more)
				group = matcher.group(3);
				String type = group;
				group = matcher.group(4);
				int blockId = Integer.parseInt(group);

				scanner.nextLine();

				line = scanner.nextLine();
				matcher = this.instructionLineInFilePattern.matcher(line);
				matches = matcher.matches();
				group = matcher.group(1);
				int instructionLine = Integer.parseInt(group);
				group = matcher.group(2);
				String filename = group;
				group = matcher.group(3);
				// TODO dir
				ThreadBankConflictThreadInfo thread1 = new ThreadBankConflictThreadInfo(filename, tid1, instructionLine);

				line = scanner.nextLine();
				matcher = this.fileLineInstructionPattern.matcher(line);
				matches = matcher.matches();
				group = matcher.group(1);
				// TODO file
				group = matcher.group(2);
				// TODO line
				group = matcher.group(3);
				// TODO inst text

				line = scanner.nextLine();
				matcher = this.kernelSharedBlockPattern.matcher(line);
				matches = matcher.matches();
				if (matches) {
					group = matcher.group(1);
					// TODO not sure what I should do with this
					group = matcher.group(2);
					// TODO not sure what I should do with this
					group = matcher.group(3);
					// TODO block
					group = matcher.group(4);
					// TODO thread
				}
				else {
					matcher = this.kernelSharedBlockPattern2.matcher(line);
					matches = matcher.matches();
					group = matcher.group(1);
					group = matcher.group(2);
					group = matcher.group(3);
					// TODO not sure what the top 3 are
					group = matcher.group(4);
					// TODO block
					group = matcher.group(5);
					// TODO thread
				}

				scanner.nextLine();

				line = scanner.nextLine();
				matcher = this.instructionLineInFilePattern.matcher(line);
				matches = matcher.matches();
				group = matcher.group(1);
				instructionLine = Integer.parseInt(group);
				group = matcher.group(2);
				filename = group;
				group = matcher.group(3);
				// TODO dir path
				ThreadBankConflictThreadInfo thread2 = new ThreadBankConflictThreadInfo(filename, tid2, instructionLine);

				line = scanner.nextLine();
				matcher = this.fileLineInstructionPattern.matcher(line);
				matches = matcher.matches();
				group = matcher.group(1);
				// TODO file
				group = matcher.group(2);
				// TODO line
				group = matcher.group(3);
				// TODO inst text

				line = scanner.nextLine();
				matcher = this.kernelSharedBlockPattern.matcher(line);
				matches = matcher.matches();
				if (matches) {
					group = matcher.group(1);
					// TODO not sure what I should do with this
					group = matcher.group(2);
					// TODO not sure what I should do with this
					group = matcher.group(3);
					// TODO block
					group = matcher.group(4);
					// TODO thread
				}
				else {
					matcher = this.kernelSharedBlockPattern2.matcher(line);
					matches = matcher.matches();
					group = matcher.group(1);
					group = matcher.group(2);
					group = matcher.group(3);
					// TODO not sure what the top 3 are
					group = matcher.group(4);
					// TODO block
					group = matcher.group(5);
					// TODO thread
				}

				this.threadBankConflicts.add(new ThreadBankConflict(thread1, thread2, type, blockId));

				scanner.nextLine();
				scanner.nextLine();
				scanner.nextLine();

				line = scanner.nextLine();
				matcher = this.gkleeCapacityBankConflictPattern.matcher(line);
				matches = matcher.matches();
			}
		}
		else {
			scanner.nextLine();
			scanner.nextLine();
		}

		/********************************* Start checking memory coalescing at DeviceMemory at capability: 2.x... *************************************/

		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.capacityInstByWholeWarpPattern.matcher(line);
		matches = matcher.matches();
		if (matches) {
			while (matches) {
				group = matcher.group(1);
				int warp = Integer.parseInt(group);

				// TODO there is probably a loop over the requests.
				line = scanner.nextLine();
				matcher = this.wordSizeAccessedByThreadsPattern.matcher(line);
				matches = matcher.matches();
				group = matcher.group(1);
				int numThreads = Integer.parseInt(group);
				group = matcher.group(2);
				int request = Integer.parseInt(group);
				group = matcher.group(3);
				int totalRequests = Integer.parseInt(group);

				scanner.nextLine();

				line = scanner.nextLine();
				matcher = this.requestCoalescedIntoMemorySegmentPattern.matcher(line);
				matches = matcher.matches();
				group = matcher.group(1);
				int memorySegment = Integer.parseInt(group);
				group = matcher.group(2);
				// TODO repeat?

				this.memoryCoalescingByWholeWarpList.add(new MemoryCoalescingByWholeWarp(warp, numThreads, request, totalRequests,
						memorySegment));

				scanner.nextLine();
				scanner.nextLine();

				line = scanner.nextLine();
				matcher = this.capacityInstByWholeWarpPattern.matcher(line);
				matches = matcher.matches();
			}
		}
		else {
			scanner.nextLine();
			scanner.nextLine();
			scanner.nextLine();
			scanner.nextLine();
		}

		scanner.nextLine();
		scanner.nextLine();
		/********************************* Start checking warp divergence ****************************/

		line = scanner.nextLine();
		matcher = this.warpThreadsDivergedPattern.matcher(line);
		matches = matcher.matches();
		while (matches) {
			group = matcher.group(1);
			int warp = Integer.parseInt(group);
			List<int[]> sets = new ArrayList<int[]>();

			line = scanner.nextLine();
			matcher = this.setPattern.matcher(line);
			matches = matcher.matches();
			while (matches) {
				group = matcher.group(1);
				// TODO set#
				List<Integer> set = new ArrayList<Integer>();

				line = scanner.nextLine();
				matcher = this.threadSetPattern.matcher(line);
				matches = matcher.matches();
				scan = new Scanner(line);
				while (scan.hasNext()) {
					scan.next();
					group = scan.next();
					set.add(Integer.parseInt(group));
					scan.next();
				}

				// primitive arrays are better than lists of boxed primitives
				int[] setArray = new int[set.size()];
				for (int i = 0; i < setArray.length; i++) {
					setArray[i] = set.get(i);
				}
				sets.add(setArray);

				line = scanner.nextLine();
				matcher = this.setPattern.matcher(line);
				matches = matcher.matches();
			}

			warpDivergences.add(new WarpDivergence(sets, warp));

			// already done
			// line = scanner.nextLine();
			matcher = this.warpThreadsDivergedPattern.matcher(line);
			matches = matcher.matches();
		}

		scanner.nextLine();
		scanner.nextLine();
		scanner.nextLine();

		/****************************************** Start checking races at SharedMemory 0... *********************************************/

		line = scanner.nextLine();
		matcher = this.pathNumExploredPattern.matcher(line);
		matches = matcher.matches();
		while (!matches) {
			matcher = this.threadsWaitExplicitPattern.matcher(line);
			matches = matcher.matches();
			if (matches) {
				List<Integer> threadList = new ArrayList<Integer>();
				scan = new Scanner(line);
				scan.next();
				while (scan.hasNext()) {
					String word = scan.next();
					matcher = this.numberPattern.matcher(word);
					matches = matcher.matches();
					if (!matches) {
						break;
					}
					group = matcher.group(1);
					threadList.add(Integer.parseInt(group));
				}

				int[] threadArray = new int[threadList.size()];
				for (int i = 0; i < threadArray.length; i++) {
					threadArray[i] = threadList.get(i);
				}
				this.threadsThatWaitAtExplicitSyncThreads = threadArray;

				line = scanner.nextLine();
				matcher = this.threadsWaitReConvPointPattern.matcher(line);
				matches = matcher.matches();
				threadList = new ArrayList<Integer>();
				scan = new Scanner(line);
				scan.next();
				while (scan.hasNext()) {
					String word = scan.next();
					matcher = this.numberPattern.matcher(word);
					matches = matcher.matches();
					if (!matches) {
						break;
					}
					group = matcher.group(1);
					threadList.add(Integer.parseInt(group));
				}

				threadArray = new int[threadList.size()];
				for (int i = 0; i < threadArray.length; i++) {
					threadArray[i] = threadList.get(i);
				}
				this.threadsThatWaitAtReConvergentPoint = threadArray;
			}
			else {
				line = scanner.nextLine();
				matcher = this.acrossDifferentWarpsThreadsRacePattern.matcher(line);
				matches = matcher.matches();
				group = matcher.group(1);
				int tid1 = Integer.parseInt(group);
				group = matcher.group(2);
				int tid2 = Integer.parseInt(group);
				group = matcher.group(3);
				String type = group;

				scanner.nextLine();

				line = scanner.nextLine();
				matcher = this.instructionLineInFilePattern.matcher(line);
				matches = matcher.matches();
				group = matcher.group(1);
				int instructionLine = Integer.parseInt(group);
				group = matcher.group(2);
				String filename = group;
				group = matcher.group(3);
				// TODO dir
				ThreadBankConflictThreadInfo thread1 = new ThreadBankConflictThreadInfo(filename, tid1, instructionLine);

				line = scanner.nextLine();
				matcher = this.fileLineInstructionPattern.matcher(line);
				matches = matcher.matches();
				group = matcher.group(1);
				group = matcher.group(2);
				group = matcher.group(3);
				// TODO these mostly just repeat

				line = scanner.nextLine();
				matcher = this.kernelSharedBlockPattern.matcher(line);
				matches = matcher.matches();
				if (matches) {
					group = matcher.group(1);
					group = matcher.group(2);
					group = matcher.group(3);
					group = matcher.group(4);
					// TODO these mostly just repeat
				}
				else {
					matcher = this.kernelSharedBlockPattern2.matcher(line);
					matches = matcher.matches();
					group = matcher.group(1);
					group = matcher.group(2);
					group = matcher.group(3);
					group = matcher.group(4);
					group = matcher.group(5);
					// TODO these mostly just repeat
				}

				// do the above again for the second thread
				scanner.nextLine();

				line = scanner.nextLine();
				matcher = this.instructionLineInFilePattern.matcher(line);
				matches = matcher.matches();
				group = matcher.group(1);
				instructionLine = Integer.parseInt(group);
				group = matcher.group(2);
				filename = group;
				group = matcher.group(3);
				// TODO dir
				ThreadBankConflictThreadInfo thread2 = new ThreadBankConflictThreadInfo(filename, tid2, instructionLine);

				line = scanner.nextLine();
				matcher = this.fileLineInstructionPattern.matcher(line);
				matches = matcher.matches();
				group = matcher.group(1);
				group = matcher.group(2);
				group = matcher.group(3);
				// TODO these are repeats

				line = scanner.nextLine();
				matcher = this.kernelSharedBlockPattern.matcher(line);
				matches = matcher.matches();
				if (matches) {
					group = matcher.group(1);
					group = matcher.group(2);
					group = matcher.group(3);
					// TODO repeats
				}
				else {
					matcher = this.kernelSharedBlockPattern2.matcher(line);
					matches = matcher.matches();
					group = matcher.group(1);
					group = matcher.group(2);
					group = matcher.group(3);
					group = matcher.group(4);
					// TODO repeats
				}

				this.threadBankConflictsAcrossWarps.add(new ThreadBankConflict(thread1, thread2, type));
			}

			line = scanner.nextLine();
			matcher = this.pathNumExploredPattern.matcher(line);
			matches = matcher.matches();
		}

		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.bankConflictInstructionPattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		this.numBIs = Integer.parseInt(group);
		group = matcher.group(2);
		this.numBCs = Integer.parseInt(group);
		group = matcher.group(3);
		this.numInstructions = Integer.parseInt(group);

		line = scanner.nextLine();
		matcher = this.bankConflictWarpPattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		// TODO repeated?
		group = matcher.group(2);
		this.warpsWithBC = Integer.parseInt(group);
		group = matcher.group(3);
		this.numberOfWarps = Integer.parseInt(group);

		// TODO this might repeat for every shared memory
		line = scanner.nextLine();
		matcher = this.bankConflictSharedPattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		this.sharedMemoryIndex = Integer.parseInt(group);
		group = matcher.group(2);
		this.BIsWithBCInSharedMemory = Integer.parseInt(group);
		group = matcher.group(3);
		// TODO repeat?

		scanner.nextLine();

		// TODO this might need to repeat for more paths
		line = scanner.nextLine();
		matcher = this.averageWarpBankConflictRatePattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		this.pathIndex = Integer.parseInt(group);
		group = matcher.group(2);
		this.aveWarpBankConflictRate = Integer.parseInt(group);
		group = matcher.group(3);
		this.aveBCWarp = Integer.parseInt(group);
		group = matcher.group(4);
		this.aveWarp = Integer.parseInt(group);

		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.averageBIBankConflictRatePattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		// TODO repeat? or needs indexing by path?
		group = matcher.group(2);
		this.aveBIBankConflictRate = Integer.parseInt(group);
		group = matcher.group(3);
		this.aveBCBI = Integer.parseInt(group);
		group = matcher.group(4);
		this.aveBI = Integer.parseInt(group);

		scanner.nextLine();
		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.memoryCoalescingReadPattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		// TODO repeat?
		group = matcher.group(2);
		this.numReadInstructionsWithMC = Integer.parseInt(group);
		group = matcher.group(3);
		this.numReadInstructions = Integer.parseInt(group);

		line = scanner.nextLine();
		matcher = this.memoryCoalescingWritePattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		// TODO repeat?
		group = matcher.group(2);
		this.numWriteInstructionsWithMC = Integer.parseInt(group);
		group = matcher.group(3);
		this.numWriteInstructions = Integer.parseInt(group);

		line = scanner.nextLine();
		matcher = this.memoryCoalescingWarpsPattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		// TODO repeated?
		group = matcher.group(2);
		this.numWarpsWithMC = Integer.parseInt(group);
		group = matcher.group(3);
		// TODO repeated?

		line = scanner.nextLine();
		matcher = this.memoryCoalescingBIsPattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		this.numBIsWithMC = Integer.parseInt(group);
		group = matcher.group(2);
		// TODO repeat?

		scanner.nextLine();

		// TODO should this loop for each path?
		line = scanner.nextLine();
		matcher = this.averageWarpMemoryCoalescingRatePattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		this.aveWarpMemoryCoalescingRate = Integer.parseInt(group);
		group = matcher.group(3);
		this.aveMCWarp = Integer.parseInt(group);
		group = matcher.group(4);
		// TODO repeat?

		scanner.nextLine();

		// TODO loop for paths?
		line = scanner.nextLine();
		matcher = this.averageBIMemoryCoalescingRatePattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		this.aveBIMemoryCoalescingRate = Integer.parseInt(group);
		group = matcher.group(3);
		this.aveMCBI = Integer.parseInt(group);
		group = matcher.group(4);
		// TODO repeat?

		scanner.nextLine();
		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.warpDivergenceWarpPattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		// TODO repeat?
		group = matcher.group(2);
		this.numWarpsWithWD = Integer.parseInt(group);
		group = matcher.group(3);
		// TODO repeat?

		line = scanner.nextLine();
		matcher = this.warpDivergenceBIPattern.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		this.numBIsWithWD = Integer.parseInt(group);
		group = matcher.group(2);
		// TODO repeat?

		scanner.nextLine();

		// TODO loop for paths?
		line = scanner.nextLine();
		matcher = this.averageWarpWarpDivergenceRate.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		this.aveWarpDivergentRate = Integer.parseInt(group);
		group = matcher.group(3);
		this.aveWDWarp = Integer.parseInt(group);
		group = matcher.group(4);
		// TODO repeat?

		scanner.nextLine();

		// TODO loop for paths?
		line = scanner.nextLine();
		matcher = this.averageBIWarpDivergenceRate.matcher(line);
		matches = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		this.aveBIWarpDivergenceRate = Integer.parseInt(group);
		group = matcher.group(3);
		this.aveWDBI = Integer.parseInt(group);
		group = matcher.group(4);
		// TODO repeat?

		scanner.nextLine();
	}

	public int getConcurrencyBugCheckingLevel() {
		return concurrencyBugCheckingLevel;
	}

	public List<WarpDivergence> getWarpDivergences() {
		return warpDivergences;
	}

}
