package com.kayo.zx;

import javax.swing.SwingUtilities;

import com.kayo.zx.view.MainFrame;

public class ZXDiagramTool {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      MainFrame frame = new MainFrame();
      frame.setVisible(true);
    });
  }
}
