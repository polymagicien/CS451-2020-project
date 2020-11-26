package cs451;

public class VC {
    private int[] vc;
    
    public VC (String payload) {
        this.vc = VC.parseVC(payload);
    }

    private static int[] parseVC(String strVC) {
        String[] v = strVC.split(";");

        int[] vc = new int[v.length];
        for (int i = 0; i < v.length; i++) 
            vc[i] = Integer.valueOf(v[i]);

        return vc;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < vc.length - 1; i++) {
            s += vc[i];
            s += ";";
        }
        s += vc[vc.length - 1];

        return s;
    }
}
