public class MainApplication {

	public static void main(String[] args) {
//		Assembler assembler = new Assembler();
		OS os = new OS(100);
//		os.loadProcess("assemblyInput.asm", assembler);

		os.start();
	}
}
