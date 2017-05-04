package vm1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class AssemblyConverter {

	private int arthJumpFlag;
	private PrintWriter outPrinter;

	/**
	 * Open an output file and be ready to write content
	 * 
	 * @param fileOut
	 *            can be a directory!
	 */
	public AssemblyConverter(File fileOut) {

		try {

			outPrinter = new PrintWriter(fileOut);
			arthJumpFlag = 0;

		} catch (FileNotFoundException e) {

			e.printStackTrace();

		}

	}

	/**
	 * â€œIf the programâ€™s argument is a directory name rather than a file
	 * name, the main program should process all the .vm files in this
	 * directory. In doing so, it should use a separate Parser for handling each
	 * input file and a single AssemblyConverter for handling the output.
	 *
	 * Inform the CodeWrither that the translation of a new VM file is started
	 */
	public void setFileName(File fileOut) {

	}

	/**
	 * Write the assembly code that is the translation of the given arithmetic
	 * command
	 * 
	 * @param command
	 */
	public void memoryAcessCommands(String command) {

		switch (command) {
		case ("add"):
			outPrinter.print(arithmeticCode() + "M=M+D\n@SP\nM=M+1\n");
			break;

		case ("sub"):
			outPrinter.print(arithmeticCode() + "M=M-D\n@SP\nM=M+1\n");
			break;

		case ("and"):
			outPrinter.print(arithmeticCode() + "M=M&D\n@SP\nM=M+1\n");
			break;

		case ("or"):
			outPrinter.print(arithmeticCode() + "M=M|D\n@SP\nM=M+1\n");
			break;

		case ("gt"):
			outPrinter.print(jumpCode("JLE"));// not <=
			arthJumpFlag++;

			break;
		case ("lt"):
			outPrinter.print(jumpCode("JGE"));// not >=
			arthJumpFlag++;
			break;

		case ("eq"):
			outPrinter.print(jumpCode("JNE"));// not <>
			arthJumpFlag++;

			break;
		case ("not"):
			outPrinter.print("@SP\nA=M\nM=!M\n");
			break;

		case ("neg"):
			outPrinter.print("D=0\n@SP\nA=M\nM=D-M\n");

			break;
		default:
			throw new IllegalArgumentException("Call memoryAcessCommands() for a non-arithmetic command");
		}
	}

	/**
	 * Write the assembly code that is the translation of the given command
	 * where the command is either PUSH or POP
	 * 
	 * @param command
	 *            PUSH or POP
	 * @param segment
	 * @param index
	 */
	public void writePushPop(int command, String segment, int index) {

		if (command == Parser.PUSH) {

			if (segment.equals("constant")) {

				outPrinter.print("@" + index + "\n" + "D=A\n@SP\nAM=M-1\nM=D\n");

			} else if (segment.equals("local")) {

				outPrinter.print(pushCode("LCL", index, false));

			} else if (segment.equals("argument")) {

				outPrinter.print(pushCode("ARG", index, false));

			} else if (segment.equals("this")) {

				outPrinter.print(pushCode("THIS", index, false));

			} else if (segment.equals("that")) {

				outPrinter.print(pushCode("THAT", index, false));

			} else if (segment.equals("temp")) {

				outPrinter.print(pushCode("R5", index + 5, false));

			} else if (segment.equals("pointer") && index == 0) {

				outPrinter.print(pushCode("THIS", index, true));

			} else if (segment.equals("pointer") && index == 1) {

				outPrinter.print(pushCode("THAT", index, true));

			} else if (segment.equals("static")) {

				outPrinter.print(pushCode(String.valueOf(16 + index), index, true));

			}

		} else if (command == Parser.POP) {

			if (segment.equals("local")) {

				outPrinter.print(popCode("LCL", index, false));

			} else if (segment.equals("argument")) {

				outPrinter.print(popCode("ARG", index, false));

			} else if (segment.equals("this")) {

				outPrinter.print(popCode("THIS", index, false));

			} else if (segment.equals("that")) {

				outPrinter.print(popCode("THAT", index, false));

			} else if (segment.equals("temp")) {

				outPrinter.print(popCode("R5", index + 5, false));

			} else if (segment.equals("pointer") && index == 0) {

				outPrinter.print(popCode("THIS", index, true));

			} else if (segment.equals("pointer") && index == 1) {

				outPrinter.print(popCode("THAT", index, true));

			} else if (segment.equals("static")) {

				outPrinter.print(popCode(String.valueOf(16 + index), index, true));

			}

		} else {

			throw new IllegalArgumentException("Call writePushPop() for a non-pushpop command");

		}

	}

	/**
	 * Close the output file
	 */
	public void close() {

		outPrinter.close();

	}

	/**
	 * Template for add sub and or
	 * 
	 * @return
	 */
	private String arithmeticCode() { // change the method name

		return "@SP\n" + "A=M\n" + // +
				"D=M\n" + "A=A+1\n";// +

	}

	/**
	 * Template for gt lt eq
	 * 
	 * @param type
	 *            JLE JGT JEQ
	 * @return
	 */
	private String jumpCode(String type) {// change the method name

		return 
				"@SP\n" + 
				"A=M\n" + 
				"D=M\n" + 
				"A=A+1\n" +
				"D=M-D\n" +
				"@FALSE" + arthJumpFlag + "\n" + 
				"D;" + type+ "\n" + 
				"@SP\n" + 
				"A=M+1\n"
				+ "M=-1\n"
				+ "@CONTINUE" + arthJumpFlag + "\n" 
				+ "0;JMP\n" 
				+ "(FALSE"+ arthJumpFlag + ")\n"
				+ "@SP\n" +
				"A=M+1\n" + 
				"M=0\n" + 
				"(CONTINUE" + arthJumpFlag + ")\n" + 
				"@SP\n" + 
				"M=M+1\n";

	}

	/**
	 * Template for push local,this,that,argument,temp,pointer,static
	 * 
	 * @param segment
	 * @param index
	 * @param isDirect
	 *            Is this command a direct addressing?
	 * @return
	 */
	private String pushCode(String segment, int index, boolean isDirect) {

		// When it is a pointer, just read the data stored in THIS or THAT
		// When it is static, just read the data stored in that address
		String noPointerCode = (isDirect) ? "" : "@" + index + "\n" + "A=D+A\nD=M\n";// look
																						// down
																						// in
																						// return
		return 
				"@" + segment + "\n" + 
				"D=M\n" + 
				noPointerCode + "@SP\n" + // next																		// available																		// index
				"AM=M-1\n" + 
				"M=D\n";
	}

	/**
	 * Template for pop local,this,that,argument,temp,pointer,static
	 * 
	 * @param segment
	 * @param index
	 * @param isDirect
	 *            Is this command a direct addressing?
	 * @return
	 */
	private String popCode(String segment, int index, boolean isDirect) {

		// When it is a pointer R13 will store the address of THIS or THAT
		// When it is a static R13 will store the index address
		String noPointerCode = (isDirect) ? "D=A\n" : "D=M\n@" + index + "\nD=D+A\n";

		return "@" + segment + "\n" + 
				noPointerCode +
				"@R13\n" + 
				"M=D\n" 
				+ "@SP\n" 
				+ "A=M\n" 
				+ "D=M\n" 
				+ "@R13\n"
				+ "A=M\n" 
				+ "M=D\n"
				+ "@SP\n"
				+ "M=M+1\n";

	}

}
