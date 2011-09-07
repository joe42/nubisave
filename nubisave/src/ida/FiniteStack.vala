//  
//  Copyright (C) 2011 Rico Tzschichholz
// 
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
// 
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
// 
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
// 

//
// Cleversafe open-source code header - Version 1.2 - February 15, 2008
//
// Cleversafe Dispersed Storage(TM) is software for secure, private and
// reliable storage of the world's data using information dispersal.
//
// Copyright (C) 2005-2008 Cleversafe, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
// USA.
//
// Contact Information: Cleversafe, 224 North Desplaines Street, Suite 500 
// Chicago IL 60661
// email licensing@cleversafe.org
//
// END-OF-HEADER

/*
 * FiniteStack.java
 *
 * @author   Hakim Weatherspoon
 * @version  $Id: FiniteStack.java,v 1.4 2004/05/14 00:46:18 hweather Exp $
 *
 *  Copyright (c) 2001 Regents of the University of California.  All
 *  rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *  3. Neither the name of the University nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS
 *  IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *  FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE
 *  REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

using Gee;

namespace NubiSave.IDA
{
	/**
	 * A simple Stack of finite size.
	 * 
	 * @author Hakim Weatherspoon
	 * @version $Id: FiniteStack.java,v 1.4 2004/05/14 00:46:18 hweather Exp $
	 */
	public class FiniteStack
	{
		private int STACK_SIZE;
		private Object[] _stack;
		private int _head;

		/**
		* Constructs a new <code>Stack</code>.
		*/
		public FiniteStack(int size)
		{
			STACK_SIZE = size;
			_head = 0;
			_stack = new Object[STACK_SIZE];
		}

		private bool isFull()
		{
			return (_head == STACK_SIZE);
		}

		private bool isEmpty()
		{
			return (_head == 0);
		}

		public bool push(Object element)
		{
			lock (_stack)
			{
				if (isFull())
					return false;
	
				_stack[_head] = element;
				_head = _head + 1;
	
				return true;
			}
		}

		public Object? pop()
		{
			lock (_stack)
			{
				if (isEmpty())
					return null;
				
				_head = _head - 1;
				Object element = _stack[_head];
				_stack[_head] = null;
				
				return element;
			}
		}

		public int size()
		{
			lock (_stack)
			{
				return _head;
			}
		}

		/**
		* Returns a human-readable representation of this
		* <code>CacheReserveState</code>.
		*/
		public string to_string()
		{
			string str = "";
			str += "<FiniteStack";
			str += " size==" + _head.to_string ();
			str += " stackCapacity==" + STACK_SIZE.to_string ();
			//for (int i = 0; i < _head; i++)
			//   str += " stack[" + i.to_string () + "]==" + _stack[i].;
			str += ">";

			return str;
		}
	}
}