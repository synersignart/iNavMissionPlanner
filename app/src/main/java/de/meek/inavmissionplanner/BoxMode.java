package de.meek.inavmissionplanner;

public class BoxMode {

//    public String m_name;
    public int m_index;
    public int m_permanentId;
    public int m_aux;
    public int m_start;
    public int m_end;

    public BoxMode(int index, int permanentId, int aux, int start, int end) {
//        this.m_name="";
        this.m_index = index;
        this.m_permanentId = permanentId;
        this.m_aux = aux;
        this.m_start = start;
        this.m_end = end;
    }

//    public String getName() {
//        return m_name;
//    }
}
