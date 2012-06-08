package havocx42;

public class TranslationRecordFactory {
	
	public static TranslationRecord createTranslationRecord(String sourceString,
			String targetString) {
		if(sourceString.contains("->")||targetString.contains("->"))return null;
		int sourceSpaceIndex;
		Integer source;
		String sourceName;
		if (sourceString.contains(" ")) {
			sourceSpaceIndex = sourceString.indexOf(' ');
			source = Integer.valueOf(sourceString
					.substring(0, sourceSpaceIndex));
			sourceName = sourceString.substring(sourceSpaceIndex);
		} else {
			source = Integer.valueOf(sourceString);
			sourceName = "";
		}

		int targetSpaceIndex;
		Integer target;
		String targetName;
		if (targetString.contains(" ")) {
			targetSpaceIndex = targetString.indexOf(' ');
			target = Integer.valueOf(targetString
					.substring(0, targetSpaceIndex));
			targetName = targetString.substring(targetSpaceIndex);
		} else {
			target = Integer.valueOf(targetString);
			targetName = "";
		}

		if (target != null && source != null) {
			return new TranslationRecord(source, target, sourceName, targetName);
		} else {
			return null;
		}
	}
	
	public static TranslationRecord createTranslationRecord(String current) {
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
