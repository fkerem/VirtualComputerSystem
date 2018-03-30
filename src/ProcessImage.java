import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class ProcessImage {
	String processName;

	public int S0;
	public int S1;
	public int S2;
	public int S3;
	public int S4;
	public int S5;
	public int S6;
	public int S7;
	public int $0;
	public int PC;
	public int V;
	public int IR;
	public int BR;
	public int LR;

	public ProcessImage() {
		this.processName = "";
		S0 = 0;
		S1 = 0;
		S2 = 0;
		S3 = 0;
		S4 = 0;
		S5 = 0;
		S6 = 0;
		S7 = 0;
		$0 = 0;
		PC = 0;
		V = 0;
		IR = 0;
		BR =0;
		LR = 0;
	}

	public ProcessImage(String processName,int baseRegister, int limitRegister ) {
		this();
		this.processName = processName;
		BR = baseRegister;
		LR = limitRegister;
	}

	public ProcessImage(int baseRegister, int limitRegister ) {
		this("Unnamed Process!", baseRegister, limitRegister);
	}

	public void writeToDumpFile() {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("processRegisterDump.bin"), StandardCharsets.UTF_8))) {
			writer.write("S0 "+S0 +"\n");
			writer.write("S1 "+S1 +"\n");
			writer.write("S2 "+S2 +"\n");
			writer.write("S3 "+S3 +"\n");
			writer.write("S4 "+S4 +"\n");
			writer.write("S5 "+S5 +"\n");
			writer.write("S6 "+S6 +"\n");
			writer.write("S7 "+S7 +"\n");
			writer.write("$0 "+$0 +"\n");
			writer.write("PC "+PC +"\n");
			writer.write("V "+V +"\n");
			writer.write("IR "+IR +"\n");
			writer.write("BR "+BR +"\n");
			writer.write("LR "+LR +"\n");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
