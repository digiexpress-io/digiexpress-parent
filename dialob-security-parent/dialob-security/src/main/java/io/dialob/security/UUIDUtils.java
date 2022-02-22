/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.security;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.UUID;

public final class UUIDUtils {

  private UUIDUtils() {
  }

  public static UUID toUUID(@Nonnull byte[] oid) {
    if (oid.length != 16) {
      throw new IllegalArgumentException("UUID is 16 bytes long. oid is " + oid.length + " bytes.");
    }
    ByteBuffer bb = ByteBuffer.wrap(oid);
    long firstLong = bb.getLong();
    long secondLong = bb.getLong();
    return new UUID(firstLong, secondLong);
  }


  public static byte[] toBytes(@Nonnull UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return bb.array();
  }

}
