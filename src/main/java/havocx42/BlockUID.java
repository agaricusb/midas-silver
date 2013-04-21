package havocx42;

public class BlockUID {
    
    public Integer blockID;
    public Integer dataValue;
    
    /**
     * Creates a new BlockUID
     * @param blockID The Block ID
     * @param dataValue The data value, also known as the damage value
     */
    public BlockUID(Integer blockID,Integer dataValue){
        this.blockID=blockID;
        this.dataValue=dataValue;
    }
    @Override
    public boolean equals(Object o) {
        if(o instanceof BlockUID){
            BlockUID b= (BlockUID)o;
            boolean result = b.blockID.equals(this.blockID);
            if(b.dataValue!=null&&this.dataValue!=null){
                result = result&b.dataValue.equals(this.dataValue);
            }
            return result;
        }else{
            return false;
        }
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(blockID);
        if(dataValue!=null){
            sb.append(":");
            sb.append(dataValue);
        }
        return sb.toString();
    }
    
    @Override
    public int hashCode() {
        return blockID;
    }

}
