package audiorecorder;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * This class creates GUI and implements methods for audio recording
 * @author Pavel Dusek
 */
public class AudioRecorder extends JFrame implements ActionListener {
    private static final int HOW_MANY_BUTTONS = 5;
    private static final float SAMPLE_RATE = 8000.0f; //8kHz
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    private ByteArrayOutputStream out;
    private AudioRecorderTask audioRecorderTask;
    private AudioFormat format = new AudioFormat( SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);

    /** 
     * This constructor creates JFrame based GUI with {@link JButton} buttons
     */
    public AudioRecorder() {
        super("AudioRecorder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1,HOW_MANY_BUTTONS));
        
        JButton play = makeButton("Play");
        add(play);
        JButton record = makeButton("Record");
        add(record);
        JButton stop = makeButton("Stop");
        add(stop);
        JButton save = makeButton("Save");
        add(save);
        JButton plot = makeButton("Plot");
        add(plot);
    }
    
    private JButton makeButton(String name) {
        JButton button = new JButton(name);
        button.addActionListener(this);
        button.setActionCommand(name);
        return button;
    }
    
    public static void main(String[] args) {
        AudioRecorder audioRecorder = new AudioRecorder();
        audioRecorder.setSize(500,100);
        audioRecorder.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("Play")) {
            play();
        } else if (ae.getActionCommand().equals("Stop")) {
            stop();
        } else if (ae.getActionCommand().equals("Record")) {
            record();
        } else if (ae.getActionCommand().equals("Save")) {
            save();
        } else if (ae.getActionCommand().equals("Plot")) {
            plot();
        }
    }

    private void play() {
        byte[] audio = out.toByteArray();
        InputStream in = new ByteArrayInputStream(audio);
        AudioInputStream ais = new AudioInputStream(in, format, audio.length / format.getFrameSize());
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try {
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(format);
            speaker.start();
            int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
            byte[] buffer = new byte[bufferSize];
            int count;
            while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
                if (count > 0) {
                    speaker.write(buffer, 0, count);
                }
            }
            speaker.drain();
            speaker.close();
        } catch (Exception excp) {
            excp.printStackTrace();
            JOptionPane.showMessageDialog(this, excp.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        }
        
    }

    private void stop() {
        audioRecorderTask.cancel(true);
        audioRecorderTask = null;
        JOptionPane.showMessageDialog(this, "Recording stopped.", "Info!", JOptionPane.INFORMATION_MESSAGE);
    }

    private class AudioRecorderTask extends SwingWorker<Void,byte[]> {

        @Override
        protected Void doInBackground() {
            TargetDataLine microphone;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Error! Audio System line not supported.");
            } else {
                //TODO open mixer's getLine()?
                try {
                    microphone = AudioSystem.getTargetDataLine(format);
                    microphone.open(format);
                    out = new ByteArrayOutputStream();
                    int numBytesRead;
                    byte[] data = new byte[microphone.getBufferSize()/5];
                    microphone.start();
                    while (!isCancelled()) {
                        numBytesRead = microphone.read(data, 0, data.length);
                        out.write(data, 0, numBytesRead);
                    }
                    out.close();
                } catch (Exception excp) {
                    System.out.println("Error! Could not open Audio System line!");
                    excp.printStackTrace();
                }
            }
            return null;
        }
    }
    /*
     * Method that reads data from the microphone port.
     */
    private void record() {
        (audioRecorderTask = new AudioRecorderTask()).execute();
    }

    /**
     * Method that takes care of saving the audio data into a file.
     */
    private void save() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Method that plots the soundwaves to a chart.
     */
    private void plot() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
