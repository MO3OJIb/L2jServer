/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Rnd;

/**
 * @author Nemesiss
 *
 */
public class L2SkillCreateItem extends L2Skill
{
    private final int[] _createItemId;
    private final int _createItemCount;
    private final int _randomCount;

    public L2SkillCreateItem(StatsSet set)
    {
        super(set);
        _createItemId = set.getIntegerArray("create_item_id");
        _createItemCount = set.getInteger("create_item_count", 0);
        _randomCount = set.getInteger("random_count", 1);
    }

    /**
     * @see net.sf.l2j.gameserver.model.L2Skill#useSkill(net.sf.l2j.gameserver.model.actor.L2Character, net.sf.l2j.gameserver.model.L2Object[])
     */
    @Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
    {
        if (activeChar.isAlikeDead()) return;
        if (_createItemId == null || _createItemCount == 0)
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
            activeChar.sendPacket(sm);
            return;
        }
        L2PcInstance player = (L2PcInstance) activeChar;
        if (activeChar instanceof L2PcInstance)
        {
            int count = _createItemCount + Rnd.nextInt(_randomCount);
            int rndid = Rnd.nextInt(_createItemId.length);
            giveItems(player, _createItemId[rndid], count);
        }
    }

    /**
     * @param activeChar
     * @param itemId
     * @param count
     */
    public void giveItems(L2PcInstance activeChar, int itemId, int count)
    {
        L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
        if (item == null) return;
        item.setCount(count);
        activeChar.getInventory().addItem("Skill", item, activeChar, activeChar);

        if (count > 1)
        {
            SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
            smsg.addItemName(item);
            smsg.addItemNumber(count);
            activeChar.sendPacket(smsg);
        }
        else
        {
            SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
            smsg.addItemName(item);
            activeChar.sendPacket(smsg);
        }
        ItemList il = new ItemList(activeChar, false);
        activeChar.sendPacket(il);
    }
}
