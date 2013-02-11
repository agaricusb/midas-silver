package havocx42;

public class TranslationRecord {
	public BlockUID target;
	public BlockUID source;
	public String sourceName;
	public String targetName;
	public TranslationRecord(Integer sourceID, Integer sourceDataValue, Integer targetID, Integer targetDataValue, String sourceName,
			String targetName) {
		super();
		this.target = new BlockUID(targetID,targetDataValue);
		this.source = new BlockUID(sourceID,sourceDataValue);
		this.sourceName = sourceName;
		this.targetName = targetName;
	}
	@Override
	public String toString() {
		return source +" "+ sourceName+" -> "+ target+" "+targetName;
	}

	
	public boolean equals(BlockUID b) {
		return (source!=null&&b.equals(source));
	}
	
	
	
	
	
	

}
