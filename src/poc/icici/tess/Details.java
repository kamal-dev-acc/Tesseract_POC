package poc.icici.tess;

public class Details {

	
	private String wordKey="";
	private String word="";
	private String page="";
	private int paragraph=0;
	private int lineNo=0;
	private String line="";
	
	
	/**
	 * @return the wordKey
	 */
	public String getWordKey() {
		return wordKey;
	}
	/**
	 * @param wordKey the wordKey to set
	 */
	public void setWordKey(String wordKey) {
		this.wordKey = wordKey;
	}
	/**
	 * @return the word
	 */
	public String getWord() {
		return word;
	}
	/**
	 * @param word the word to set
	 */
	public void setWord(String word) {
		this.word = word;
	}
	/**
	 * @return the page
	 */
	public String getPage() {
		return page;
	}
	/**
	 * @param page the page to set
	 */
	public void setPage(String page) {
		this.page = page;
	}
	/**
	 * @return the paragraph
	 */
	public int getParagraph() {
		return paragraph;
	}
	/**
	 * @param paragraph the paragraph to set
	 */
	public void setParagraph(int paragraph) {
		this.paragraph = paragraph;
	}
	/**
	 * @return the lineNo
	 */
	public int getLineNo() {
		return lineNo;
	}
	/**
	 * @param lineNo the lineNo to set
	 */
	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}
	/**
	 * @return the line
	 */
	public String getLine() {
		return line;
	}
	/**
	 * @param line the line to set
	 */
	public void setLine(String line) {
		this.line = line;
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Details [getWordKey()=" + getWordKey() + ", getWord()=" + getWord() + ", getPage()=" + getPage()
				+ ", getParagraph()=" + getParagraph() + ", getLineNo()=" + getLineNo() + ", getLine()=" + getLine()
				+ "]";
	}

	
}
