package us.zoom.sdksample.cmd;

import us.zoom.sdksample.IListener;

public interface CmdHandler extends IListener {
    void onCmdReceived(CmdRequest request);
}
