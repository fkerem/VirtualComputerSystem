import java.nio.ByteBuffer;

public class CPU {

	private Memory mMemory;
	String processName;
	private int S0;
	private int S1;
	private int S2;
	private int S3;
	private int S4;
	private int S5;
	private int S6;
	private int S7;
	private int $0;
	private int PC;
	private int V;
	private int IR;
	private int BR;
	private int LR;

	public CPU(Memory memory) {
		mMemory=memory;
	}

	public int decodeExecute() {
		int R1=0,R2=0;
		int r1=0,r2=0,r3=0,c=0;
		int[] binaryFormat = toBinary((int)this.IR, 32); 
		int operation = calculateSpecificLoc(binaryFormat, 0, 5);

		if (operation == 0)
		{
			return 0 ;
			//Leave it to OS
		}

		else if (operation == 16 || operation == 17)
		{
			r1 = calculateSpecificLoc(binaryFormat, 6, 10); 
			r2 = calculateSpecificLoc(binaryFormat, 11, 15);
			r3 = calculateSpecificLoc(binaryFormat, 16, 20);
			R1 = returnReg(r1);  
			R2 = returnReg(r2);
		}

		else if (operation == 2 || operation == 3 || operation == 7 || operation == 31)
		{
			r1 = calculateSpecificLoc(binaryFormat, 6, 10);
			r2 = calculateSpecificLoc(binaryFormat, 11, 15);
			R1 = returnReg(r1);
			R2 = returnReg(r2);
			c = calculateSpecificLoc(binaryFormat, 16, 31);
		}
		else if (operation == 9 || operation == 10)
		{
			r1 = calculateSpecificLoc(binaryFormat, 6, 10);
			r2 = calculateSpecificLoc(binaryFormat, 11, 15);
			R1 = returnReg(r1);
			R2 = returnReg(r2);
		}
		else if (operation == 8)
		{
			r1 = calculateSpecificLoc(binaryFormat, 6, 10);
			R1 = returnReg(r1);
			c = calculateSpecificLoc(binaryFormat, 16, 31);
		}

		if (operation == 16) {
			setRegister(r1,  returnReg(r2)+ returnReg(r3));  

		} else if (operation == 17) {
			setRegister(r1, returnReg(r2) - returnReg(r3));
		} else if (operation == 2) {
			setRegister(r1, returnReg(r2)+c);
		} else if (operation == 3) {
			setRegister(r1, returnReg(r2)-c);
		} else if (operation == 31) {
			setRegister(r1, ( returnReg(r2)|c));
		} else if (operation == 7) {
			if (R1 == R2) PC = BR + c;
		} else if (operation == 9) {
			char [] buffer = mMemory.getInstruction(R2, 0);
			R1 = ((char)buffer[0] << 24) + ((char)buffer[1] << 16) + ((char)buffer[2] << 8) + (char)buffer[3];
			setRegister(r1, ((char)buffer[0] << 24) + ((char)buffer[1] << 16) + ((char)buffer[2] << 8) + (char)buffer[3]);
		} else if(operation == 10) {
			char [] buffer = new char[4];
			byte[] bytes = ByteBuffer.allocate(4).putInt(R1).array();

			for (int i = 0; i < 4; i++) {
				int b=(int)bytes[i];
				if(b<0)
					b=b+256;
				buffer[i]= (char) b ;
			}

			mMemory.addInstructions(buffer, 4, R2);
		} else if (operation == 8) {
			R1 = (c << 16);
			setRegister(r1, c<<16);
		}
		return 1;
	}

	public void fetch() {
		char[]buffer=mMemory.getInstruction(this.PC,this.BR);
		int result=((char)buffer[0] << 24) + ((char)buffer[1] << 16) + ((char)buffer[2] << 8) + (char)buffer[3];
		this.IR = result;
		this.PC += 4;
	}

	public void transferFromImage(ProcessImage pImage) {
		this.processName = pImage.processName;
		this.S0 = pImage.S0;
		this.S1 = pImage.S1;
		this.S2 = pImage.S2;
		this.S3 = pImage.S3;
		this.S4 = pImage.S4;
		this.S5 = pImage.S5;
		this.S6 = pImage.S6;
		this.S7 = pImage.S7;
		this.$0 = pImage.$0;
		this.PC = pImage.PC;
		this.V = pImage.V;
		this.IR = pImage.IR;
		this.BR = pImage.BR;
		this.LR = pImage.LR;
	}

	public void transferToImage(ProcessImage pImage) {
		pImage.processName = this.processName;
		pImage.S0 = this.S0;
		pImage.S1 = this.S1;
		pImage.S2 = this.S2;
		pImage.S3 = this.S3;
		pImage.S4 = this.S4;
		pImage.S5 = this.S5;
		pImage.S6 = this.S6;
		pImage.S7 = this.S7;
		pImage.$0 = this.$0;
		pImage.PC = this.PC;
		pImage.V  = this.V;
		pImage.IR = this.IR;
		pImage.BR = this.BR;
		pImage.LR = this.LR;
	}

	private int calculateSpecificLoc(int[] arr, int base, int limit) {
		int power = limit - base;
		int total = 0;

		for (int i = base; i <= limit; i++, power--) {

			if (arr[i] == 1)
				total += (int) Math.pow(2, power);
		}
		return total;
	}

	private int returnReg(int num) {
		if (num == 0)
			return (this.PC);
		else if (num == 2)
			return (this.V);
		else if (num == 3)
			return (this.S0);
		else if (num == 4)
			return (this.S1);
		else if (num == 5)
			return (this.S2);
		else if (num == 6)
			return (this.S3);
		else if (num == 7)
			return (this.S4);
		else if (num == 8)
			return (this.S5);
		else if (num == 9)
			return (this.S6);
		else if (num == 10)
			return (this.S7);
		else if (num == 11)
			return (this.BR);
		else if (num == 12)
			return (this.$0);
		else
			return -1;

	}

	private void setRegister(int num,int value) {
		if (num == 0)
			this.PC=value;
		else if (num == 2)
			this.V=value;
		else if (num == 3)
			this.S0=value;
		else if (num == 4)
			this.S1=value;
		else if (num == 5)
			this.S2=value;
		else if (num == 6)
			this.S3=value;
		else if (num == 7)
			this.S4=value;
		else if (num == 8)
			this.S5=value;
		else if (num == 9)
			this.S6=value;
		else if (num == 10)
			this.S7=value;
		else if (num == 11)
			this.BR=value;
		else if (num == 12)
			this.$0=value;

	}

	private int[] toBinary(int number, int size) {
		int[] binaryForm = new int[size];
		int power = size - 1;

		for (int i = 0; i < size; i++, power--) {

			if (number - (int) Math.pow(2, power) >= 0) {
				number -= (int) Math.pow(2, power);
				binaryForm[i] = 1;
			}
			else
				binaryForm[i] = 0;
		}
		return binaryForm;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public int getS0() {
		return S0;
	}

	public void setS0(int s0) {
		S0 = s0;
	}

	public int getS1() {
		return S1;
	}

	public void setS1(int s1) {
		S1 = s1;
	}

	public int getS2() {
		return S2;
	}

	public void setS2(int s2) {
		S2 = s2;
	}

	public int getS3() {
		return S3;
	}

	public void setS3(int s3) {
		S3 = s3;
	}

	public int getS4() {
		return S4;
	}

	public void setS4(int s4) {
		S4 = s4;
	}

	public int getS5() {
		return S5;
	}

	public void setS5(int s5) {
		S5 = s5;
	}

	public int getS6() {
		return S6;
	}

	public void setS6(int s6) {
		S6 = s6;
	}

	public int getS7() {
		return S7;
	}

	public void setS7(int s7) {
		S7 = s7;
	}

	public int get$0() {
		return $0;
	}

	public void set$0(int $0) {
		this.$0 = $0;
	}

	public int getPC() {
		return PC;
	}

	public void setPC(int pC) {
		PC = pC;
	}

	public int getV() {
		return V;
	}

	public void setV(int v) {
		V = v;
	}

	public int getIR() {
		return IR;
	}

	public void setIR(int iR) {
		IR = iR;
	}

	public int getBR() {
		return BR;
	}

	public void setBR(int bR) {
		BR = bR;
	}

	public int getLR() {
		return LR;
	}

	public void setLR(int lR) {
		LR = lR;
	}
}
