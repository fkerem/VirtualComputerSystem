import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Assembler {

	public int createBinaryFile(String inputfile, String outputFile) {
		int counter = 0;
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile), StandardCharsets.UTF_8));
			OutputStreamWriter wr = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);

			String line="";

			while((line = br.readLine()) != null && line.trim().isEmpty()==false) {
				int instruction_encoded = EncodeLine(line);
				byte[] bytes = ByteBuffer.allocate(4).putInt(instruction_encoded).array();

				for (int i = 0; i < 4; i++) {
					int b=(int)bytes[i];
					if(b<0)
						b=b+256;
					wr.write( Character.toString( (char)b));
				}
				counter = counter + 4; 
			}
			wr.close();
			br.close();
			System.out.println("Byte file created...");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return counter;
	}

	public char[] readBinaryFile(int instructionSize,String fileName)
	{ 
		char [] buffer = new char[instructionSize];
		try {
			BufferedReader br= new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));

			String line="";
			while((line = br.readLine()) != null && line.trim().isEmpty()==false) {
				for(int i=0;i<instructionSize;i++) {
					buffer[0+i]=line.charAt(0+i);
				}
			}

			br.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return buffer;
	}

	private int CharSeqToInt(String S) {
		int total = 0;
		for (int i = 15; i >= 0; i--) {
			if (S.charAt(15-i)=='1')
				total += (int)Math.pow(2, i);
		}
		return total;
	}

	private int EncodeLine(String S) {
		String list[]=S.split(" ");
		String op;
		op=list[0];
		int ins = 0;
		if (op .equals("ADD")) {
			ins += (int)Math.pow(2, 30);
			return Type1(ins, S);
		}
		else if (op .equals( "SUB")) {
			ins += (int)Math.pow(2, 30) + (int)Math.pow(2, 26);
			return Type1(ins, S);
		}
		else if (op .equals( "ADDI")) {
			ins += (int)Math.pow(2, 27);
			return Type2(ins, S);
		}
		else if (op .equals( "SUBI")) {
			ins += (int)Math.pow(2, 27) + (int)Math.pow(2, 26);
			return Type2(ins, S);
		}
		else if (op .equals( "ORI")) {
			ins += (int)Math.pow(2, 29) + (int)Math.pow(2, 26) + (int)Math.pow(2, 30) + (int)Math.pow(2, 28) + (int)Math.pow(2, 27);
			return Type2(ins, S);
		}
		else if (op .equals( "LUI")) {
			ins += (int)Math.pow(2, 29);
			return Type3(ins, S);
		}
		else if (op .equals( "BEQ")) {
			ins += (int)Math.pow(2, 28) + (int)Math.pow(2, 27) + (int)Math.pow(2, 26);
			return Type2(ins, S);
		}
		else if (op .equals( "LW")) {
			ins += (int)Math.pow(2, 29) + (int)Math.pow(2, 26);
			return Type4(ins, S);
		}
		else if (op .equals( "SW")) {
			ins += (int)Math.pow(2, 29) + (int)Math.pow(2, 27);
			return Type4(ins, S);
		}
		else if (op .equals( "SYSCALL"))
			return 0;
		else
		{
			return -1; //Error Code
		}
	}

	private int getRnum(String Reg) {
		if (Reg .equals( "PC"))
			return 0;
		else if (Reg .equals( "SP"))
			return 1;
		else if (Reg .equals( "V"))
			return 2;
		else if (Reg .equals( "S0"))
			return 3;
		else if (Reg .equals( "S1"))
			return 4;
		else if (Reg .equals( "S2"))
			return 5;
		else if (Reg .equals( "S3"))
			return 6;
		else if (Reg .equals( "S4"))
			return 7;
		else if (Reg .equals( "S5"))
			return 8;
		else if (Reg .equals( "S6"))
			return 9;
		else if (Reg .equals( "S7"))
			return 10;
		else if (Reg .equals( "BR"))
			return 11;
		else if (Reg .equals( "$0"))
			return 12;
		else
		{
			return -1; // Error code
		}
	}

	private int Type1(int init, String currentLine) {
		String reg1, reg2, reg3;
		String list[]=currentLine.split(" ");
		reg1=list[1];
		reg2=list[2];
		reg3=list[3];

		return (int)(init + getRnum(reg1)*Math.pow(2, 21) + getRnum(reg2)*Math.pow(2, 16) + getRnum(reg3)*Math.pow(2, 11));
	}

	private int Type2(int init, String currentLine) {
		String reg1, reg2, imm;
		String list[]=currentLine.split(" ");
		reg1=list[1];
		reg2=list[2];
		imm=list[3];

		return (int)(init + getRnum(reg1)*Math.pow(2, 21) + getRnum(reg2)*Math.pow(2, 16) + CharSeqToInt(imm)   );
	}

	private int Type3(int init, String currentLine) {
		String reg1, imm;
		String list[]=currentLine.split(" ");
		reg1=list[1];
		imm=list[2];

		return (int)(init + getRnum(reg1)*Math.pow(2, 21) + CharSeqToInt(imm)   );
	}

	private int Type4(int init, String currentLine) {
		String reg1, reg2;
		String list[]=currentLine.split(" ");
		reg1=list[1];
		reg2=list[2];

		return (int)(init + getRnum(reg1)*Math.pow(2, 21) + getRnum(reg2)*Math.pow(2, 16)   );
	}
}
