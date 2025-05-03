package fun.rockstarity.api.render.ui.mainmenu.alt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.render.ui.mainmenu.Page;
import fun.rockstarity.api.render.ui.mainmenu.screens.AltScreen;
import fun.rockstarity.api.secure.Debugger;
import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 24 окт. 2024 г.
 */

@UtilityClass
public class BanProcessor implements IAccess {
	
	public void join(String ip) {
		try {
			for (AltServer server : getCurrentAlt().getServers()) {
				if (server.getIp().equals(ip)) {
					return;
				}
			}
			
			getCurrentAlt().getServers().add(new AltServer(ip));
		} catch (Exception e) {
			Debugger.print(e);
		}
	}
	
	public void updateBan(String ip, String message) {
		try {
			for (AltServer server : getCurrentAlt().getServers()) {
				if (server.getIp().split("\\.")[1].equals(ip.split("\\.")[1])) {
					server.setBan(message);
					getCurrentAlt().setBanned(true);
					return;
				}
			}
		} catch (Exception e) {
			Debugger.print(e);
		}
	}
	
	public boolean checkBan(Alt alt) {
        for (AltServer server : alt.getServers()) {
        	if (server.getBan() == null || server.getBan().isEmpty()) continue;
        	
        	if (server.getIp().toLowerCase().contains("funtime")) {
        		Pattern banDatePattern = Pattern.compile("время бана: (\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2})");
                Matcher banDateMatcher = banDatePattern.matcher(server.getBan());

                Pattern unbanPattern = Pattern.compile("разбан через: (\\d+) д, (\\d+) ч, (\\d+) м");
                Matcher unbanMatcher = unbanPattern.matcher(server.getBan());
                
                try {
                    if (banDateMatcher.find() && unbanMatcher.find()) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                        Date banDate = dateFormat.parse(banDateMatcher.group(1));

                        long banDuration = TimeUnit.DAYS.toMillis(Long.parseLong(unbanMatcher.group(1))) +
                                          TimeUnit.HOURS.toMillis(Long.parseLong(unbanMatcher.group(2))) +
                                          TimeUnit.MINUTES.toMillis(Long.parseLong(unbanMatcher.group(3)));

                        ZonedDateTime currentDate = ZonedDateTime.now(java.time.ZoneId.of("Europe/Moscow"));
                        Date unbanDate = new Date(banDate.getTime() + banDuration);
                        
                        if (!currentDate.toInstant().isAfter(unbanDate.toInstant())) {
                            return true;
                        } else {
                        	server.setBan("");
                        	alt.setBanned(false);
                        }
                    }
                } catch (ParseException e) {
                    Debugger.print(e);
                }
        	} else if (server.getIp().toLowerCase().contains("reallyworld") || server.getIp().toLowerCase().contains("rw")) {
        		Pattern banDatePattern = Pattern.compile("дата бана: (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})");
                Matcher banDateMatcher = banDatePattern.matcher(server.getBan());

                Pattern unbanPattern = Pattern.compile("окончание бана через: (\\d+) дней, (\\d+) часов, (\\d+) минут");
                Matcher unbanMatcher = unbanPattern.matcher(server.getBan());
                
                Pattern unbanPattern1 = Pattern.compile("окончание бана через: (\\d+) минут");
                Matcher unbanMatcher1 = unbanPattern1.matcher(server.getBan());

                try {
                	boolean find = banDateMatcher.find();
                	if (find && unbanMatcher.find()) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date banDate = dateFormat.parse(banDateMatcher.group(1));

                        long banDuration = TimeUnit.DAYS.toMillis(Long.parseLong(unbanMatcher.group(1))) +
                                TimeUnit.HOURS.toMillis(Long.parseLong(unbanMatcher.group(2))) +
                                TimeUnit.MINUTES.toMillis(Long.parseLong(unbanMatcher.group(3)));

                        ZonedDateTime currentDate = ZonedDateTime.now(java.time.ZoneId.of("Europe/Moscow"));
                        Date unbanDate = new Date(banDate.getTime() + banDuration);

                        if (!currentDate.toInstant().isAfter(unbanDate.toInstant())) {
                            return true;
                        } else {
                            server.setBan("");
                            alt.setBanned(false);
                        }
                    }

                	if (find && unbanMatcher1.find()) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date banDate = dateFormat.parse(banDateMatcher.group(1));

                        long banDuration = TimeUnit.DAYS.toMillis(0) +
                                TimeUnit.HOURS.toMillis(0) +
                                TimeUnit.MINUTES.toMillis(Long.parseLong(unbanMatcher1.group(1)));
                        
                        ZonedDateTime currentDate = ZonedDateTime.now(java.time.ZoneId.of("Europe/Moscow"));
                        Date unbanDate = new Date(banDate.getTime() + banDuration);
                       
                        if (!currentDate.toInstant().isAfter(unbanDate.toInstant())) {
                            return true;
                        } else {
                            server.setBan("");
                            alt.setBanned(false);
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
        	}
        }
        
        return false;
	}
	
	public Alt getCurrentAlt() throws Exception {
		List<Alt> alts = ((AltScreen) Page.ALT.getScreen()).getAlts();
		
		for (Alt alt : alts) {
			if (alt.getUsername().equals(mc.getSession().getUsername()))
				return alt;
		}
		
		Alt alt = null;
		alts.add(alt = new Alt(mc.getSession().getUsername(), false));
		return alt;
		//throw new Exception("альт нулл хуета");
	}

}
