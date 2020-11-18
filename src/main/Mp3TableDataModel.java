package main;

import com.beaglebuddy.mp3.MP3;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Mp3TableDataModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = {"Path", "Title", "Artists", "Changed"};
    private final String path;
    private ArrayList<ChangeableMp3File> files;

    public Mp3TableDataModel(String path) {
        super();
        this.path = path;
        reloadSongs();
    }

    private void reloadSongs() {
        files = new ArrayList<>();
        addPath(new File(path));
        fireTableDataChanged();
    }

    private void addPath(File file) {
        String[] filenames = file.list((dir, name) -> name.endsWith(".mp3"));
        if (filenames != null) {
            for (String filename : filenames) {
                try {
                    files.add(new ChangeableMp3File(file.getPath() + "\\" + filename));
                } catch (IOException ioException) {
                    System.out.println("Could not add " + filename);
                }
            }
        }
        File[] subdirs = file.listFiles(File::isDirectory);
        if (subdirs != null) {
            for (File subdir : subdirs) {
                addPath(subdir);
            }
        }
    }

    @Override
    public int getRowCount() {
        return files.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex < 3 ? String.class : Boolean.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 3 && files.get(rowIndex).isChanged()) return true;
        return columnIndex > 0 && columnIndex < 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ChangeableMp3File file = files.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return file.getPath();
            case 1:
                return file.getCurrentTitle();
            case 2:
                return file.getCurrentArtists();
            case 3:
                return file.isChanged();
            default:
                throw new IllegalStateException("Unexpected value: " + columnIndex);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        ChangeableMp3File file = files.get(rowIndex);
        switch (columnIndex) {
            case 1:
                file.setCurrentTitle((String) aValue);
                break;
            case 2:
                file.setCurrentArtists((String) aValue);
                break;
            case 3:
                file.reset();
                break;
        }
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public void saveAll() {
        for (ChangeableMp3File file : files) {
            try {
                file.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        reloadSongs();
    }

    public void resetAll() {
        for (ChangeableMp3File file : files) {
            file.reset();
        }
        fireTableDataChanged();
    }

    public void autoFill() {
        for (ChangeableMp3File file : files) {
            String filename = new File(file.getPath()).getName();
            int pos = filename.indexOf(" - ");
            if (pos != -1) {
                String artists = filename.substring(0, pos);
                String title = filename.substring(pos + 3);
                title = title.replaceAll("\\.mp3", "");
                artists = artists
                        .replaceAll(" & ", "/")
                        .replaceAll(", ", "/")
                        .replaceAll(" X ", "/")
                        .replaceAll(" x ", "/");
                file.setCurrentArtists(artists);
                file.setCurrentTitle(title);
            }
        }
        fireTableDataChanged();
    }

    static class ChangeableMp3File extends MP3 {
        private String artists, title;
        private boolean changed;

        public ChangeableMp3File(String path) throws IOException {
            super(path);
            reset();
        }

        public void reset() {
            title = getTitle();
            artists = getLeadPerformer();
            if (title == null) {
                title = "";
            }
            if (artists == null) {
                artists = "";
            }
            reevaluateChanged();
        }

        private void reevaluateChanged() {
            String tempTitle = getTitle();
            String tempArtist = getLeadPerformer();
            if (tempTitle == null) {
                tempTitle = "";
            }
            if (tempArtist == null) {
                tempArtist = "";
            }
            changed = !(tempTitle.equals(title) && tempArtist.equals(artists));
        }

        @Override
        public void save() throws IOException {
            clear();
            if (!title.equals(""))
                setTitle(title);
            if (!artists.equals(""))
                setLeadPerformer(artists);
            super.save();
        }

        public String getCurrentArtists() {
            return artists;
        }

        public void setCurrentArtists(String artists) {
            this.artists = artists;
            reevaluateChanged();
        }

        public String getCurrentTitle() {
            return title;
        }

        public void setCurrentTitle(String title) {
            this.title = title;
            reevaluateChanged();
        }

        public boolean isChanged() {
            return changed;
        }
    }
}
