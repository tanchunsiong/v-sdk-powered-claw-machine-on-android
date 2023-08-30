package us.zoom.sdksample;

import java.util.Vector;

public class ListenerList {
    private Vector<IListener> mList;

    public ListenerList () {
        mList = new Vector<IListener>();
    }

    public int add(IListener l) {
        int size = 0;
        synchronized(mList) {
            if(l != null && !mList.contains(l)) {
                mList.add(l);
            }
            size = mList.size();
        }
        return size;
    }

    public int remove(IListener l) {
        int size = 0;
        synchronized(mList) {
            if(l != null) {
                mList.remove(l);
            }
            size = mList.size();
        }
        return size;
    }

    public IListener[] getAll() {
        IListener[] contents = null;

        synchronized(mList) {
            contents = new IListener[mList.size()];
            mList.toArray(contents);
        }

        return contents;
    }

    public void clear()
    {
        mList.clear();
    }

    public int size() {
        synchronized(mList) {
            return mList.size();
        }
    }
}
