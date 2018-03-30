public class Memory {

	private int memorySize;
	private char[] memory;
//	private int emptyIndex;
	private char[] bitmap;

	public Memory(int size) {
		memorySize = size;
		memory = new char[size];
		bitmap = new char[size];
		for(int i=0; i<size; i++) {
			bitmap[i] = 0;
		}

//		emptyIndex = 0;
	}

	void addInstructions(char[] buffer, int bufferSize, int BR)
	{
		for (int i = BR; i < bufferSize+BR; i++)
		{
			this.memory[i] = buffer[i - BR];
			this.bitmap[i] = 1;
		}

//		emptyIndex += bufferSize;
	}


	char[]getInstruction(int PC, int BR)
	{
		char[]instruction = new char[4];
		instruction[0]=memory[PC+BR];
		instruction[1]=memory[PC+BR+1];
		instruction[2]=memory[PC+BR+2];
		instruction[3]=memory[PC+BR+3];

		return instruction;

	}
/*
	int getEmptyIndex()
	{
		return this.emptyIndex;
	}
*/
	public int getMemorySize() {
		return memorySize;
	}
	
	public int availableIndex(int instSize) { //Returns -1 if memory is not sufficiently empty!
		int count=instSize;
		int index = -1;
		boolean firstTime = true;
		for(int i=0; i<memorySize; i++) {
			if(count == 0)
				break;
			else if(bitmap[i] == 0) {
				if(firstTime == true) {
					index = i;
					firstTime = false;
				}
				count--;
			}
			else if(bitmap[i] == 1) {
				count = instSize;
				index = -1;
				firstTime = true;
			}
		}
		if(count != 0)
			index = -1;
		
		return index;
	}
	
	public void deallocate(int B, int instSize) {
		for(int i=0; i<instSize; i++) {
			bitmap[i] = 0;
		}
	}

}
