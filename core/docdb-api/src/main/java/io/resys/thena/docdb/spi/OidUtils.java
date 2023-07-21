package io.resys.thena.docdb.spi;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.annotation.Nullable;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import io.resys.thena.docdb.api.exceptions.RepoException;


public final class OidUtils {

  private OidUtils() { }

  private static final SecureRandom SECURE_RANDOM;

  static {
    SecureRandom secureRandom;
    try {
      secureRandom = SecureRandom.getInstance("NativePRNGNonBlocking");
    }
    catch (NoSuchAlgorithmException e) {
      try {
        secureRandom = SecureRandom.getInstanceStrong();
      }
      catch (NoSuchAlgorithmException e1) {
        secureRandom = new SecureRandom();
      }
    }
    SECURE_RANDOM = secureRandom;
  }

  public static byte[] generateOID() {
    byte[] oid = new byte[16];
    SECURE_RANDOM.nextBytes(oid);
    return oid;
  }

  public static String toString(byte[] oid) {
    return Hex.encodeHexString(oid);
  }
  
  public static String gen() {
    return toString(generateOID());
  }

  @Nullable
  public static byte[] toOID(@Nullable String id) {
    if (id == null) {
      return null;
    }
    try {
      id = id.replace("-", "");
      byte[] oidBytes = Hex.decodeHex(id.toCharArray());
      if (oidBytes.length > 16) {
        throw new RepoException(id + " is too long ID");
      }
      if (oidBytes.length < 16) {
        oidBytes = Arrays.copyOf(oidBytes, 16);
      }
      return oidBytes;
    } catch (DecoderException e) {
      throw new RepoException(id + " is not valid ID: " + e.getMessage());
    }
  }

  @Nullable
  static Integer validateRevValue(@Nullable String rev) {
    if (rev == null) {
      return null;
    }
    if (StringUtils.isBlank(rev) || !StringUtils.isNumeric(rev)) {
      throw new IllegalArgumentException("rev must be numeric");
    }
    return Integer.parseInt(rev);
  }
}
