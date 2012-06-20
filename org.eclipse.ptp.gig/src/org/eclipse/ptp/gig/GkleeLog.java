package org.eclipse.ptp.gig;

import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GkleeLog {
	// the member variables parsed from the log file
	private final int concurrencyBugCheckingLevel;

	// the regular expressions for parsing the log file
	private final Pattern concurrencyBugcheckingLevelPattern = Pattern
			.compile("Configuration: concurrency bug checking level is: (\\d+)"); //$NON-NLS-1$
	private final Pattern deadlockCheckingAtBlockPattern = Pattern.compile("Deadlock Checking at Block (\\d+): "); //$NON-NLS-1$
	private final Pattern startCheckingBankConflictsAtSharedMemoryPattern = Pattern
			.compile("\\** Start checking bank conflicts at SharedMemory (\\d+)... \\**"); //$NON-NLS-1$
	private final Pattern capacityInstByWholeWarpPattern = Pattern
			.compile("GKLEE: \\** CAPACITY 2.x Inst By Whole Warp \\( (\\d+) \\) \\**"); //$NON-NLS-1$
	private final Pattern wordSizeAccessedByThreadsPattern = Pattern
			.compile("GKLEE: The word size accessed by threads: (\\d+), the (\\d+)th request over total (\\d+) requests"); //$NON-NLS-1$
	private final Pattern requestCoalescedIntoMemorySegmentPattern = Pattern
			.compile("GKLEE: This request is coalesced into one memory segment \\(128 Bytes\\) accessed by (\\d+) threads"); //$NON-NLS-1$
	private final Pattern warpThreadsDivergedPattern = Pattern
			.compile("In warp (\\d+), threads are diverged into following sub-sets: "); //$NON-NLS-1$
	private final Pattern setPattern = Pattern.compile("Set (\\d+):"); //$NON-NLS-1$
	private final Pattern threadSetPattern = Pattern.compile("(Thread (\\d+) , )+"); //$NON-NLS-1$
	private final Pattern threadsWaitExplicitPattern = Pattern
			.compile("Threads \\((\\d+, )+\\) wait at the explicit __syncthreads\\(\\)"); //$NON-NLS-1$
	private final Pattern numberPattern = Pattern.compile("\\(?(\\d+),"); //$NON-NLS-1$
	private final Pattern threadsWaitReConvPointPattern = Pattern
			.compile("Threads \\((\\d+, )+\\) wait at the re-convergence point, not __syncthreads\\(\\)"); //$NON-NLS-1$
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

	public GkleeLog(InputStream logInputStream) {
		// TODO look at code in gklee to find out exactly how it prints the log file, the format, etc.
		Scanner scanner = new Scanner(logInputStream);
		Scanner scan;

		String line = scanner.nextLine();
		Matcher matcher = this.concurrencyBugcheckingLevelPattern.matcher(line);
		// TODO b is mostly for debug purposes at this moment (except where used for program flow)
		boolean b = matcher.matches();
		// TODO group needs to be parsed and put into variables as needed
		String group = matcher.group(1);
		this.concurrencyBugCheckingLevel = Integer.parseInt(group);

		scanner.nextLine();
		scanner.nextLine();
		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.deadlockCheckingAtBlockPattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);

		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.startCheckingBankConflictsAtSharedMemoryPattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);

		scanner.nextLine();
		scanner.nextLine();
		scanner.nextLine();
		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.capacityInstByWholeWarpPattern.matcher(line);
		b = matcher.matches();
		while (b) {
			group = matcher.group(1);

			line = scanner.nextLine();
			matcher = this.wordSizeAccessedByThreadsPattern.matcher(line);
			b = matcher.matches();
			group = matcher.group(1);
			group = matcher.group(2);
			group = matcher.group(3);

			scanner.nextLine();

			line = scanner.nextLine();
			matcher = this.requestCoalescedIntoMemorySegmentPattern.matcher(line);
			b = matcher.matches();
			group = matcher.group(1);

			scanner.nextLine();
			scanner.nextLine();

			line = scanner.nextLine();
			matcher = this.capacityInstByWholeWarpPattern.matcher(line);
			b = matcher.matches();
		}

		scanner.nextLine();
		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.warpThreadsDivergedPattern.matcher(line);
		b = matcher.matches();
		while (b) {
			group = matcher.group(1);

			line = scanner.nextLine();
			matcher = this.setPattern.matcher(line);
			b = matcher.matches();
			while (b) {
				group = matcher.group(1);

				line = scanner.nextLine();
				matcher = this.threadSetPattern.matcher(line);
				b = matcher.matches();
				scan = new Scanner(line);
				while (scan.hasNext()) {
					scan.next();
					group = scan.next();
					scan.next();
				}

				line = scanner.nextLine();
				matcher = this.setPattern.matcher(line);
				b = matcher.matches();
			}

			// already done
			// line = scanner.nextLine();
			matcher = this.warpThreadsDivergedPattern.matcher(line);
			b = matcher.matches();
		}

		scanner.nextLine();
		scanner.nextLine();
		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.threadsWaitExplicitPattern.matcher(line);
		b = matcher.matches();
		scan = new Scanner(line);
		scan.next();
		while (scan.hasNext()) {
			String word = scan.next();
			matcher = this.numberPattern.matcher(word);
			b = matcher.matches();
			if (!b) {
				break;
			}
			group = matcher.group(1);
		}

		line = scanner.nextLine();
		matcher = this.threadsWaitReConvPointPattern.matcher(line);
		b = matcher.matches();
		scan = new Scanner(line);
		scan.next();
		while (scan.hasNext()) {
			String word = scan.next();
			matcher = this.numberPattern.matcher(word);
			b = matcher.matches();
			if (!b) {
				break;
			}
			group = matcher.group(1);
		}

		scanner.nextLine();
		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.bankConflictInstructionPattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);

		line = scanner.nextLine();
		matcher = this.bankConflictWarpPattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);

		line = scanner.nextLine();
		matcher = this.bankConflictSharedPattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);

		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.averageWarpBankConflictRatePattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);
		group = matcher.group(4);

		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.averageBIBankConflictRatePattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);
		group = matcher.group(4);

		scanner.nextLine();
		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.memoryCoalescingReadPattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);

		line = scanner.nextLine();
		matcher = this.memoryCoalescingWritePattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);

		line = scanner.nextLine();
		matcher = this.memoryCoalescingWarpsPattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);

		line = scanner.nextLine();
		matcher = this.memoryCoalescingBIsPattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);

		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.averageWarpMemoryCoalescingRatePattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);
		group = matcher.group(4);

		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.averageBIMemoryCoalescingRatePattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);
		group = matcher.group(4);

		scanner.nextLine();
		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.warpDivergenceWarpPattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);

		line = scanner.nextLine();
		matcher = this.warpDivergenceBIPattern.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);

		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.averageWarpWarpDivergenceRate.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);
		group = matcher.group(4);

		scanner.nextLine();

		line = scanner.nextLine();
		matcher = this.averageBIWarpDivergenceRate.matcher(line);
		b = matcher.matches();
		group = matcher.group(1);
		group = matcher.group(2);
		group = matcher.group(3);
		group = matcher.group(4);

		scanner.nextLine();
	}

}
