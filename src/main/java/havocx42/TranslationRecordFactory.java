package havocx42;

public class TranslationRecordFactory {
	
	public static TranslationRecord createTranslationRecord(String sourceString,
			String targetString) {
		if(sourceString.contains("->")||targetString.contains("->"))return null;
		int sourceSpaceIndex;
		Integer sourceBlockID=null;
		Integer sourceDataValue;
		String sourceName;
		String[] parts;
		boolean includesData =sourceString.contains(":"); 
		if(includesData){
			parts= sourceString.split(":");
			sourceString=parts[1];
			sourceBlockID=Integer.valueOf(parts[0]);
		}
		if (sourceString.contains(" ")) {
			sourceSpaceIndex = sourceString.indexOf(' ');
			sourceName = sourceString.substring(sourceSpaceIndex).trim();
			sourceString = sourceString.substring(0, sourceSpaceIndex);
		} else {
			sourceName = "";
		}
		if(includesData){
			sourceDataValue=Integer.valueOf(sourceString);
		}else{
			sourceBlockID=Integer.valueOf(sourceString);
			sourceDataValue=null;
		}
		//target
		int targetSpaceIndex;
		Integer targetBlockID=null;
		Integer targetDataValue;
		String targetName;
		includesData =targetString.contains(":"); 
		if(includesData){
			parts= targetString.split(":");
			targetString=parts[1];
			targetBlockID=Integer.valueOf(parts[0]);
		}
		if (targetString.contains(" ")) {
			targetSpaceIndex = targetString.indexOf(' ');
			targetName = targetString.substring(targetSpaceIndex).trim();
			targetString = targetString.substring(0, targetSpaceIndex);
		} else {
			targetName = "";
		}
		if(includesData){
			targetDataValue=Integer.valueOf(targetString);
		}else{
			targetBlockID=Integer.valueOf(targetString);
			targetDataValue=null;
		}

		if (targetBlockID != null && sourceBlockID != null) {
			return new TranslationRecord(sourceBlockID, sourceDataValue, targetBlockID,targetDataValue, sourceName, targetName);
		} else {
			return null;
		}
	}
	
	public static TranslationRecord createTranslationRecord(String current) {
        if (current.contains("#")) {
            current = current.substring(0, current.indexOf("#"));
        }

		if (current.contains("->")) {
			int index = current.indexOf("->");
			String currentSource = current.substring(0, index );
			String currentTarget = current.substring(index + 3);
			return createTranslationRecord(currentSource, currentTarget);
		} else {
			return null;
		}

	}

}
