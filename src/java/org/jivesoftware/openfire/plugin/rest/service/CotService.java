package org.jivesoftware.openfire.plugin.rest.service;

import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import uy.com.ipcontact.plugin.groupmanager.GroupManagerPlugin;
import uy.com.ipcontact.plugin.groupmanager.external.ExternalConnection;

@Path("restapi/v1/ipcontact/cot/{entryPoint}/{username}")
public class CotService {
	private static final Logger log = LoggerFactory.getLogger(CotService.class);
	private GroupManagerPlugin groupManager;
	private Map<UUID, JID> idMap;

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		PluginManager pm = XMPPServer.getInstance().getPluginManager();
		groupManager = (GroupManagerPlugin)pm.getPlugin("groupmanager");
		idMap = (Map<UUID, JID>)groupManager.privateData.get("cot");
	}

	@POST
	public Response sendMedia(@PathParam("entryPoint") String entryPoint, @PathParam("username") String username,
			@QueryParam("url") String url, @QueryParam("caption") String caption) {
		JID jid = idMap.remove(UUID.fromString(username));
		Response ret = Response.status(Response.Status.NOT_FOUND).build();

		if (jid != null) {
			for (ExternalConnection ext : groupManager.extConnections) {
				if (ext.isClientOrigin(jid)) {
					try {
						ext.sendMedia(jid.toBareJID(), url, "");
						ret = Response.status(Response.Status.OK).build();
					}
					catch (Exception e) {
						log.info("Unable to send message", e);
					}
					break;
				}
			}
		}

		return ret;
	}
}
