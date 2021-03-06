import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;

public class AwesomeServerHandler extends SimpleChannelInboundHandler<UpdatePacket> {
	
	private static final ChannelGroup channels = new DefaultChannelGroup(null);
	
	public AwesomeServerHandler(boolean autoRelease) {
		super(autoRelease);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		channels.add(ctx.channel());
		System.out.println("ABDEBUG: added channel: " + ctx.name() + " to group.");
		
		//Send initial gameState to client upon connection.
		GameServerState state = GameServerState.getInstance();
		System.out.println(state.updateClients().toString());
		ChannelFuture f = ctx.writeAndFlush(state.updateClients());
		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				System.out.println("Finished connection write");
			}
		});
	}
	
//	@Override
//	public void channelClosed(ChannelHandlerContext ctx, Object msg) {
//    	
//    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, UpdatePacket msg) {
		 
			System.out.println("ABDEBUG: in serverhandler channelRead!\nUpdatePacket:\n");
		 
	        UpdatePacket m = (UpdatePacket) msg;
	        System.out.println(m.toString());
	        
	        GameServerState state = GameServerState.getInstance();
	        
	        //Handle update move from player
	        state.handlePlayerMove(m);
	        //Generate new packet to send back to all clients
	        UpdatePacket upToAll = state.updateClients();
	        //Send packet to all clients.
	        channels.flushAndWrite(upToAll);
	        
	        if(upToAll.isGameEnd()) {
	        	channels.close();
	        	System.out.println("ABDEBUG: Closing channel");
	        }        	
		}
}
