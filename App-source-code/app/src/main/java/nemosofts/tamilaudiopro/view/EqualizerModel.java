package nemosofts.tamilaudiopro.view;

public class EqualizerModel {

    private int[] seekbarpos = new int[5];
    private int presetPos;
    private short reverbPreset;
    private short bassStrength;

    public EqualizerModel() {
        reverbPreset = -1;
        bassStrength = -1;
    }

    public int[] getSeekbarpos() {
        return seekbarpos;
    }

    public void setSeekbarpos(int[] seekbarpos) {
        this.seekbarpos = seekbarpos;
    }

    public int getPresetPos() {
        return presetPos;
    }

    public void setPresetPos(int presetPos) {
        this.presetPos = presetPos;
    }

    public short getReverbPreset() {
        return reverbPreset;
    }

    public void setReverbPreset(short reverbPreset) {
        this.reverbPreset = reverbPreset;
    }

    public short getBassStrength() {
        return bassStrength;
    }

    public void setBassStrength(short bassStrength) {
        this.bassStrength = bassStrength;
    }
}
