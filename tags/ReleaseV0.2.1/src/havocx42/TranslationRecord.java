package havocx42;

public class TranslationRecord {
	public Integer target;
	public Integer source;
	public String sourceName;
	public String targetName;
	public TranslationRecord(Integer source, Integer target, String sourceName,
			String targetName) {
		super();
		this.target = target;
		this.source = source;
		this.sourceName = sourceName;
		this.targetName = targetName;
	}
	@Override
	public String toString() {
		return source +" "+ sourceName+" -> "+ target+" "+targetName;
	}

	
	public boolean equals(int i) {
		return (source!=null&&i==source);
	}
	
	
	
	
	
	

}
