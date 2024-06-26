package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Objects;
import java.util.List;

import javax.swing.JPanel;

import controller.IFeatures;
import model.Day;
import model.Event;
import model.IEvent;
import model.IUser;
import model.ReadOnlyPlannerModel;

/**
 * This panel is used to view a users schedule for the week. It shows 7 columns
 * denoting a day of the week from Sunday to Saturday. In each column there
 * is 24 rows denoting a hour of the day. Within this panel there are other
 * panels denoting events at the specified time and day as seen through the view and
 * gridlines. These are EventRedPanel and see the javadoc in that class for more information.
 * This panel is updated when a new user is selected from the main bottom panel.
 *
 * @implNote This is package protected because there should not be
 *           any leakage of the WeekViewPanel information and this should be contained in the view.
 */
class WeekViewPanel extends JPanel {
  private final ReadOnlyPlannerModel model;
  private final String selectedUsername;

  private Rectangle bounds;

  private IFeatures feature;

  private boolean hostView;



  /**
   * This is the constructor for a weekViewPanel which takes in the model for which
   * it is trying to view and the selected user whose schedule is being viewed.
   *
   * @param model    the given model being viewed
   * @param selectedUsername the selected user for the view.
   */
  WeekViewPanel(ReadOnlyPlannerModel model, String selectedUsername, boolean hostView) {
    this.model = Objects.requireNonNull(model);
    this.selectedUsername = selectedUsername;
    this.hostView = hostView;
  }


  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g.create();
    super.paintComponent(g2d);
    this.bounds = getBounds();
    this.removeAll();
    setSize(this.bounds.width, this.bounds.height);
    for (IEvent e : model.selectSchedule(this.selectedUsername)) {
      drawEvent(e);
    }
    drawLines(g2d);
  }

  /**
   * This method draws the gridlines to give the user a better visualization
   * of the time at when events are taking place. They utilize bounds so that
   * they stay proportional even after window resizing.
   *
   * @param g2d the graphics object which the lines are being drawn.
   */
  private void drawLines(Graphics2D g2d) {
    AffineTransform old = g2d.getTransform();
    int horizontalLineOffset = this.bounds.height / 23;
    for (int line = horizontalLineOffset; line < this.bounds.height;
         line += horizontalLineOffset) {
      if ((line % (horizontalLineOffset * 4)) == 0) {
        g2d.setStroke(new BasicStroke(4));
      } else {
        g2d.setStroke(new BasicStroke(2));
      }
      g2d.setColor(Color.BLACK);
      g2d.drawLine(0, line, this.bounds.width, line);
    }
    int verticalLineOffset = this.bounds.width / 7;
    for (int line = verticalLineOffset; line < this.bounds.width; line += verticalLineOffset) {
      g2d.setColor(Color.BLACK);
      g2d.drawLine(line, 0, line, this.bounds.height);
    }
    g2d.setTransform(old);
  }

  /**
   * This is the method that takes a given event and creates an EventRedPanel for the
   * user to view on this panel in its respective place on the WeekViewPanel. This
   * method takes in the event it is trying to display and the graphic through which it can draw
   *
   * @param e the given event trying to be displayed.
   */
  private void drawEvent(IEvent e) {
    java.util.List<Day> daysOrder = java.util.List.of(Day.Sunday,
            Day.Monday, Day.Tuesday, Day.Wednesday, Day.Thursday,
            Day.Friday, Day.Saturday);
    int verticalLineOffset = this.bounds.width / 7;
    int horizontalLineOffset = this.bounds.height / 23;
    int start = (e.observeStartTimeOfEvent() / 100) * horizontalLineOffset;
    int end = this.bounds.height;
    if (e.observeStartDayOfEvent().equals(e.observeEndDayOfEvent())) {
      end = (e.observeEndTimeOfEvent() / 100) * horizontalLineOffset;
    } else {
      drawEndOfEvent(e, e.observeStartDayOfEvent());
    }
    this.add(colorDecorator(e,
            daysOrder.indexOf(e.observeStartDayOfEvent()) * verticalLineOffset, start,
            verticalLineOffset, end - start,
            horizontalLineOffset, convertToStringArray(model.getListOfUser()),
            this.selectedUsername, this.feature));

  }

  /**
   * This method converts a given list of users to an array of users
   * to use in the JComboBox and select users. This is used in the
   * mouse clicked method which opens the event dialogue box with the
   * list of users in the event.
   *
   * @param users the list of users in the event
   * @return a mirroring array of users
   */
  private static String[] convertToStringArray(List<IUser> users) {
    String[] usernames = new String[users.size()];
    for (int index = 0; index < users.size(); index++) {
      usernames[index] = users.get(index).toString();
    }
    return usernames;
  }

  /**
   * This method is an extension of the drawEvent method and continues to draw
   * the event if the event spans several days. This is a different but similar
   * method because it draws the event but due to how event spanning more than one day
   * are it draws the event from the top till end time or fills the entire day.
   *
   * @param e            the event being continued to be drawn.
   * @param lastDayDrawn the previous day of the event drawn.
   */
  private void drawEndOfEvent(IEvent e, Day lastDayDrawn) {
    java.util.List<Day> daysOrder = List.of(Day.Sunday,
            Day.Monday, Day.Tuesday, Day.Wednesday, Day.Thursday,
            Day.Friday, Day.Saturday);
    int verticalLineOffset = this.bounds.width / 7;
    int horizontalLineOffset = this.bounds.height / 23;
    if (daysOrder.get(daysOrder.indexOf(lastDayDrawn) + 1).equals(e.observeEndDayOfEvent())) {
      int end = (e.observeEndTimeOfEvent() / 100) * horizontalLineOffset;
      this.add(colorDecorator(e,
              daysOrder.indexOf(e.observeEndDayOfEvent()) * verticalLineOffset, 0,
              verticalLineOffset, end,
              horizontalLineOffset, convertToStringArray(model.getListOfUser()),
              this.selectedUsername, this.feature));
    } else {
      this.add(colorDecorator(e,
              (daysOrder.indexOf(lastDayDrawn) + 1) * verticalLineOffset, 0,
              verticalLineOffset, this.bounds.height,
              horizontalLineOffset, convertToStringArray(model.getListOfUser()),
              this.selectedUsername, this.feature));
      drawEndOfEvent(e, daysOrder.get(daysOrder.indexOf(lastDayDrawn) + 1));
    }
  }

  void addFeature(IFeatures features){
    this.feature = features;
  }


  EventPanel colorDecorator(IEvent e, int x, int y, int width, int height, int horiz,
                            String[] availUsers, String selected, IFeatures features){
    if(this.hostView && this.selectedUsername.equals(e.observeHost().toString())){
      return new EventBluePanel(e, x, y, width, height, horiz, availUsers,
              selected,features);
    }else{
      return new EventRedPanel(e, x, y, width, height, horiz, availUsers,
              selected,features);
    }
  }

}